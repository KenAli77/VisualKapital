package com.example.visualmoney.data.repository

import com.example.visualmoney.data.local.AssetType
import com.example.visualmoney.data.local.CachedQuoteDao
import com.example.visualmoney.data.local.CachedQuoteEntity
import com.example.visualmoney.data.local.PortfolioBuyDao
import com.example.visualmoney.data.local.toAsset
import com.example.visualmoney.data.remote.FmpDataSource
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

private const val QUOTE_TTL_MS = 5 * 60 * 60 * 1000L // 5 hours

// Data class to combine portfolio position with live quote data
data class PortfolioHoldingWithQuote(
    val symbol: String,
    val type: AssetType,
    val totalQuantity: Double,
    val costBasis: Double,
    val avgCost: Double,
    val currentPrice: Double,
    val changePct: Double,
    val dayLow: Double?,
    val dayHigh: Double?,
    val name: String?
)

interface FinancialRepository {
    suspend fun getQuote(symbol: String): AssetQuote
    suspend fun getQuotes(symbols: List<String>): List<AssetQuote>
    suspend fun getProfile(symbol: String): AssetProfile
    suspend fun getCommodities(): List<AssetQuote>
    suspend fun getChart(symbol: String): List<ChartPoint>
    fun observePortfolioWithQuotes(): Flow<List<PortfolioHoldingWithQuote>>
}

class FinancialRepositoryImpl(
    private val remoteSource: FmpDataSource,
    private val cachedQuoteDao: CachedQuoteDao,
    private val portfolioBuyDao: PortfolioBuyDao
) : FinancialRepository {
    override suspend fun getQuote(symbol: String): AssetQuote {
        val now = Clock.System.now().toEpochMilliseconds()
        val cached = cachedQuoteDao.get(symbol)
        val isFresh = cached != null && (now - cached.updatedAtEpochMs) < QUOTE_TTL_MS
        if (isFresh) return cached.toAsset()
        val remote = remoteSource.getQuote(symbol)
        val entity = CachedQuoteEntity(
            symbol = remote.symbol,
            price = remote.price,
            changePct = remote.changePercentage,
            change = remote.change,
            dayLow = remote.dayLow,
            dayHigh = remote.dayHigh,
            yearLow = remote.yearLow,
            yearHigh = remote.yearHigh,
            volume = remote.volume,
            marketCap = remote.marketCap,
            exchange = remote.exchange,
            eps = remote.eps,
            pe = remote.pe,
            earningsAnnouncement = remote.earningsAnnouncement,
            sharesOutstanding = remote.sharesOutstanding,
            updatedAtEpochMs = now
        )
        cachedQuoteDao.upsert(entity)
        return remote
    }

    override suspend fun getQuotes(symbols: List<String>): List<AssetQuote> {
        val now = Clock.System.now().toEpochMilliseconds()
        val cutoff = now - QUOTE_TTL_MS

        // If you can, add this DAO method for efficiency:
        // @Query("SELECT * FROM cached_quotes WHERE symbol IN (:symbols)")
        // suspend fun getMany(symbols: List<String>): List<CachedQuoteEntity>
        val cachedList = symbols.mapNotNull { cachedQuoteDao.get(it) } // simple but N DB calls
        val cachedBySymbol = cachedList.associateBy { it.symbol }

        val fresh = cachedList
            .filter { it.updatedAtEpochMs >= cutoff }
            .map { it.toAsset() }

        val staleOrMissingSymbols = symbols.filter { sym ->
            val c = cachedBySymbol[sym]
            c == null || c.updatedAtEpochMs < cutoff
        }

        if (staleOrMissingSymbols.isEmpty()) return fresh

        val remote = remoteSource.getQuotes(staleOrMissingSymbols)
        cachedQuoteDao.upsertAll(remote.map { it.toCachedQuoteEntity(updatedAt = now) })

        return fresh + remote

    }

    override suspend fun getProfile(symbol: String): AssetProfile = remoteSource.getProfile(symbol)

    override suspend fun getCommodities(): List<AssetQuote> = remoteSource.getCommodities()
    override suspend fun getChart(symbol: String): List<ChartPoint> = remoteSource.getChart(symbol)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePortfolioWithQuotes(): Flow<List<PortfolioHoldingWithQuote>> {
        println("FinancialRepository: Setting up observePortfolioWithQuotes flow")
        return portfolioBuyDao.observePositions()
            .flatMapLatest { positions ->
                println("FinancialRepository: Got ${positions.size} positions, refreshing stale quotes first...")
                
                // First, refresh any stale quotes for portfolio symbols
                if (positions.isNotEmpty()) {
                    val symbols = positions.map { it.symbol }
                    refreshStaleQuotesForSymbols(symbols)
                }
                
                // After refresh completes, observe the cache (which now has fresh data)
                cachedQuoteDao.observeAll().map { quotes ->
                    println("FinancialRepository: Combining ${positions.size} positions with ${quotes.size} quotes")
                    
                    val quotesBySymbol = quotes.associateBy { it.symbol }
                    positions.map { position ->
                        val quote = quotesBySymbol[position.symbol]
                        println("FinancialRepository: ${position.symbol} -> price=${quote?.price ?: 0.0}, changePct=${quote?.changePct ?: 0.0}")
                        
                        PortfolioHoldingWithQuote(
                            symbol = position.symbol,
                            type = AssetType.valueOf(position.type),
                            totalQuantity = position.totalQuantity,
                            costBasis = position.costBasis,
                            avgCost = position.avgCost,
                            currentPrice = quote?.price ?: 0.0,
                            changePct = quote?.changePct ?: 0.0,
                            dayLow = quote?.dayLow,
                            dayHigh = quote?.dayHigh,
                            name = null
                        )
                    }
                }
            }
    }

    /**
     * Checks which quotes are stale (>5 hours) or missing and refreshes them from API.
     */
    private suspend fun refreshStaleQuotesForSymbols(symbols: List<String>) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val cutoff = now - QUOTE_TTL_MS

            // Get cached quotes for these symbols
            val cachedQuotes = symbols.mapNotNull { cachedQuoteDao.get(it) }
            val cachedBySymbol = cachedQuotes.associateBy { it.symbol }

            // Find stale or missing symbols
            val staleOrMissingSymbols = symbols.filter { sym ->
                val cached = cachedBySymbol[sym]
                val isStale = cached == null || cached.updatedAtEpochMs < cutoff
                if (isStale) {
                    val age = cached?.let { (now - it.updatedAtEpochMs) / (60 * 60 * 1000) } ?: -1
                    println("FinancialRepository: Symbol $sym is stale (age: ${age}h) or missing, will refresh")
                }
                isStale
            }

            if (staleOrMissingSymbols.isEmpty()) {
                println("FinancialRepository: All quotes are fresh, no API call needed")
                return
            }

            println("FinancialRepository: Fetching ${staleOrMissingSymbols.size} stale quotes from API: $staleOrMissingSymbols")

            // Fetch from API
            val freshQuotes = remoteSource.getQuotes(staleOrMissingSymbols)
            println("FinancialRepository: Received ${freshQuotes.size} quotes from API")

            // IMPORTANT: Only update cache if API returned data, otherwise keep stale cache
            if (freshQuotes.isEmpty()) {
                println("FinancialRepository: API returned empty response, keeping existing cache data")
                return
            }

            // Update cache with fresh data
            val entities = freshQuotes.map { it.toCachedQuoteEntity(updatedAt = now) }
            cachedQuoteDao.upsertAll(entities)
            println("FinancialRepository: Updated cache with ${entities.size} fresh quotes")

        } catch (e: Exception) {
            // Don't fail the flow if API call fails, just log and use cached data
            println("FinancialRepository: Error refreshing quotes from API: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun refreshStaleQuotesForPortfolio(symbols: List<String>) {
        val now = Clock.System.now().toEpochMilliseconds()
        val cutoff = now - QUOTE_TTL_MS

        val stale = cachedQuoteDao.getStale(cutoff)
            .map { it.symbol }
            .toSet()

        val toRefresh = symbols.filter { it in stale }

        if (toRefresh.isEmpty()) return

        // Batch call if your API supports it
        val quotes = remoteSource.getQuotes(toRefresh)

        val entities = quotes.map { q ->
            CachedQuoteEntity(
                symbol = q.symbol,
                price = q.price,
                changePct = q.changePercentage,
                change = q.change,
                dayLow = q.dayLow,
                dayHigh = q.dayHigh,
                yearLow = q.yearLow,
                yearHigh = q.yearHigh,
                volume = q.volume,
                marketCap = q.marketCap,
                currency = q.exchange,
                updatedAtEpochMs = now
            )
        }

        cachedQuoteDao.upsertAll(entities)
    }
}

private fun AssetQuote.toCachedQuoteEntity(updatedAt: Long): CachedQuoteEntity {
    return CachedQuoteEntity(
        symbol = symbol,
        price = price,
        changePct = changePercentage,
        change = change,
        dayLow = dayLow,
        dayHigh = dayHigh,
        yearLow = yearLow,
        yearHigh = yearHigh,
        volume = volume,
        marketCap = marketCap,
        exchange = exchange,
        eps = eps,
        pe = pe,
        earningsAnnouncement = earningsAnnouncement,
        sharesOutstanding = sharesOutstanding,
        updatedAtEpochMs = updatedAt,
    )
}
