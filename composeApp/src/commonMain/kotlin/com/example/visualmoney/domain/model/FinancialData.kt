package com.example.visualmoney.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed interface Asset {
    val symbol: String
    val name: String?
}

@Serializable
data class Stock(override val symbol: String, override val name: String?, val exchange: String? = null) : Asset
@Serializable
data class Crypto(override val symbol: String, override val name: String?) : Asset
@Serializable
data class Commodity(override val symbol: String, override val name: String?) : Asset
@Serializable
data class ETF(override val symbol: String, override val name: String?) : Asset
@Serializable
data class Forex(override val symbol: String, override val name: String?) : Asset

@Serializable
data class AssetProfile(
    val symbol: String ="",
    val price: Double = 0.0,
    val beta: Double? = null,
    val volAvg: Long? = null,
    val mktCap: Double? = null,
    val lastDiv: Double? = null,
    val range: String? = null,
    val changes: Double? = null,
    val companyName: String? = null,
    val currency: String? = null,
    val csin: String? = null,
    val isin: String? = null,
    val cusip: String? = null,
    val exchange: String? = null,
    val exchangeShortName: String? = null,
    val industry: String? = null,
    val website: String? = null,
    val description: String? = null,
    val ceo: String? = null,
    val sector: String? = null,
    val country: String? = null,
    val fullTimeEmployees: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val dcfDiff: Double? = null,
    val dcf: Double? = null,
    val image: String? = null,
    val ipoDate: String? = null,
    val defaultImage: Boolean? = null,
    val isEtf: Boolean? = null,
    val isActivelyTrading: Boolean? = null,
    val isAdr: Boolean? = null,
    val isFund: Boolean? = null
)
fun AssetProfile.toAsset(): Asset {
    return Stock(
        symbol = symbol,
        name = companyName,
        exchange = exchange
    )
}
@Serializable
data class AssetQuote(
    val symbol: String = "",
    val name: String? = null,
    val price: Double = 0.0,
    val changesPercentage: Double = 0.0,
    val change: Double = 0.0,
    val dayLow: Double? = null,
    val dayHigh: Double? = null,
    val yearHigh: Double? = null,
    val yearLow: Double? = null,
    val marketCap: Double? = null,
    val priceAvg50: Double? = null,
    val priceAvg200: Double? = null,
    val exchange: String? = null,
    val volume: Long? = null,
    val avgVolume: Long? = null,
    val open: Double? = null,
    val previousClose: Double? = null,
    val eps: Double? = null,
    val pe: Double? = null,
    val earningsAnnouncement: String? = null,
    val sharesOutstanding: Long? = null,
    val timestamp: Long? = null
)

@Serializable
data class ChartPointDTO(
    val date: String,
    val symbol: String,
    val price: Double,
    val volume: Long
)
fun ChartPointDTO.toChartPoint(): ChartPoint{
    return ChartPoint(
        date = LocalDate.parse(date),
        symbol = symbol,
        price = price,
        volume = volume
    )
}
data class ChartPoint(
    val date: LocalDate,
    val symbol: String,
    val price: Double,
    val volume: Long

)
