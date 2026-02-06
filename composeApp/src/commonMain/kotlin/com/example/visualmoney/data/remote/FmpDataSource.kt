package com.example.visualmoney.data.remote


import com.example.visualmoney.BuildKonfig
import com.example.visualmoney.data.local.SearchResult
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import com.example.visualmoney.domain.model.ChartPointDTO
import com.example.visualmoney.domain.model.toChartPoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class FmpDataSource(private val client: HttpClient) {
    private val baseUrl = "https://financialmodelingprep.com"
    private val apiKey = BuildKonfig.FMP_API_KEY

    suspend fun getQuote(symbol: String): AssetQuote {
        return try {
            val url = "$baseUrl/stable/quote?symbol=$symbol"
            val response: HttpResponse = client.get(url) {
                parameter("apikey", apiKey)
            }
            val responseText = response.bodyAsText()
            println("FmpDataSource.getQuote($symbol): Raw response = ${responseText.take(500)}")
            
            val json = Json { ignoreUnknownKeys = true }
            val quotes = json.decodeFromString<List<AssetQuote>>(responseText)
            val quote = quotes.firstOrNull() ?: AssetQuote()
            println("FmpDataSource.getQuote($symbol): Parsed - price=${quote.price}, changesPercentage=${quote.changePercentage}")
            quote
        } catch (e: Exception) {
            println("FmpDataSource.getQuote($symbol): ERROR - ${e.message}")
            AssetQuote()
        }
    }

    suspend fun getQuotes(symbols: List<String>): List<AssetQuote> {
        if (symbols.isEmpty()) return emptyList()
        
        println("FmpDataSource.getQuotes: Fetching ${symbols.size} symbols individually (batch endpoint requires premium)")
        
        // Make individual API calls for each symbol since batch endpoint requires premium subscription
        val results = mutableListOf<AssetQuote>()
        for (symbol in symbols) {
            try {
                val quote = getQuote(symbol)
                if (quote.symbol.isNotEmpty()) {
                    println("FmpDataSource.getQuotes: Got $symbol -> price=${quote.price}, changePct=${quote.changePercentage}")
                    results.add(quote)
                } else {
                    println("FmpDataSource.getQuotes: No data for $symbol")
                }
            } catch (e: Exception) {
                println("FmpDataSource.getQuotes: Error fetching $symbol: ${e.message}")
            }
        }
        
        println("FmpDataSource.getQuotes: Successfully fetched ${results.size}/${symbols.size} quotes")
        return results
    }

    suspend fun getProfile(symbol: String): AssetProfile {
        return try {
            println("Getting profile for $baseUrl/stable/profile?symbol=$symbol ")
            val response: List<AssetProfile> = client.get("$baseUrl/stable/profile?symbol=$symbol") {
                parameter("apikey", apiKey)
            }.body()
             response.firstOrNull() ?: AssetProfile()

        } catch (e: Exception) {
            println("Error getting profile: $e")
            AssetProfile()
        }
    }

    suspend fun getChart(symbol: String, from:String, to: String): List<ChartPoint> {
        return try {
            val response: List<ChartPointDTO> =
                client.get("$baseUrl/stable/historical-price-eod/light?symbol=$symbol") {
                    parameter("apikey", apiKey)
                }.body()
            response.map { it.toChartPoint() }.takeLast(100)

        } catch (e: Exception) {
            println("Exception getting char: $e")
            emptyList()
        }
    }

    suspend fun searchCompanyByName(query:String, exchange: String = "") : List<SearchResult> {
        return try {
            val response = client.get("$baseUrl/stable/search-name?query=$query") {
                parameter("apikey", apiKey)
                parameter("limit",10)
                exchange.takeIf { it.isNotEmpty() }?.let {
                    parameter("exchange", it)
                }
            }.body<List<SearchResult>>()
            println("Result from search: $response")

            response
        } catch (e: Exception) {
            println("Exception getting search: $e")
            emptyList()
        }
    }
    suspend fun getTopGainers(): List<AssetQuote> {
        val url = "$baseUrl/stable/most-actives"
        println("FMPDataSource: Request URL = $url?apikey=$apiKey")

        val response: HttpResponse = client.get(url) {
            parameter("apikey", apiKey)
        }

        val responseText = response.bodyAsText()
        println("FMPDataSource: Response = ${responseText.take(200)}...") // Log first 200 chars

        if (responseText.trim().startsWith("{")) {
            try {
                val json = Json { ignoreUnknownKeys = true }
                val errorResponse = json.decodeFromString<FmpErrorResponse>(responseText)
                println("FMP API Error: ${errorResponse.errorMessage}")
                throw Exception("FMP API Error: ${errorResponse.errorMessage}")
            } catch (e: Exception) {
                println("FMPDataSource: Failed to parse error response: ${e}")
                throw Exception("FMP API returned an error: $responseText")
            }
        }

        return try {
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<AssetQuote>>(responseText)
        } catch (e: Exception) {
            println("FMPDataSource: Failed to parse response: ${e.message}")
            emptyList()
        }
    }

    private fun handleFmpError(exception: Exception) {
        val errorMessage = exception.message ?: "Unknown error"

        if (errorMessage.contains("Error Message")) {
            val regex = """"Error Message":\s*"([^"]+)"""".toRegex()
            val match = regex.find(errorMessage)
            if (match != null) {
                val fmpError = match.groupValues[1]
                println("FMP API Error: $fmpError")
                throw Exception("FMP API Error: $fmpError")
            }
        }

        println("FMP API Error (unparsed): $errorMessage")
    }

    suspend fun getTopLosers(): List<AssetQuote> {
        // Using current stable endpoint instead of legacy /api/v3/stock_market/losers
        return client.get("$baseUrl/stable/biggest-losers") {
            parameter("apikey", apiKey)
        }.body()
    }

    suspend fun getCommodities(): List<AssetQuote> {
        return client.get("$baseUrl/stable/batch-commodity-quotes") {
            parameter("apikey", apiKey)
        }.body()
    }

    suspend fun getCrypto(): List<SearchResult> {
        return client.get("$baseUrl/stable/cryptocurrency-list") {
            parameter("apikey", apiKey)
        }.body()
    }

    // -------- Calendar Endpoints --------

    suspend fun getEarningsCalendar(from: String, to: String): List<EarningsCalendarDto> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v3/earning_calendar") {
                parameter("from", from)
                parameter("to", to)
                parameter("apikey", apiKey)
            }
            val responseText = response.bodyAsText()
            println("FmpDataSource.getEarningsCalendar: Response = ${responseText.take(200)}...")

            if (responseText.trim().startsWith("{")) {
                println("FmpDataSource.getEarningsCalendar: Received error object")
                return emptyList()
            }

            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<EarningsCalendarDto>>(responseText)
        } catch (e: Exception) {
            println("FmpDataSource.getEarningsCalendar: ERROR - ${e.message}")
            emptyList()
        }
    }

    suspend fun getDividendCalendar(from: String, to: String): List<DividendCalendarDto> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v3/stock_dividend_calendar") {
                parameter("from", from)
                parameter("to", to)
                parameter("apikey", apiKey)
            }
            val responseText = response.bodyAsText()
            println("FmpDataSource.getDividendCalendar: Response = ${responseText.take(200)}...")

            if (responseText.trim().startsWith("{")) {
                println("FmpDataSource.getDividendCalendar: Received error object")
                return emptyList()
            }

            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<DividendCalendarDto>>(responseText)
        } catch (e: Exception) {
            println("FmpDataSource.getDividendCalendar: ERROR - ${e.message}")
            emptyList()
        }
    }

    suspend fun getStockSplitCalendar(from: String, to: String): List<StockSplitCalendarDto> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/api/v3/stock_split_calendar") {
                parameter("from", from)
                parameter("to", to)
                parameter("apikey", apiKey)
            }
            val responseText = response.bodyAsText()
            println("FmpDataSource.getStockSplitCalendar: Response = ${responseText.take(200)}...")

            if (responseText.trim().startsWith("{")) {
                println("FmpDataSource.getStockSplitCalendar: Received error object")
                return emptyList()
            }

            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString<List<StockSplitCalendarDto>>(responseText)
        } catch (e: Exception) {
            println("FmpDataSource.getStockSplitCalendar: ERROR - ${e.message}")
            emptyList()
        }
    }
}
