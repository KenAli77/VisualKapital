package com.example.visualmoney.domain.model

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
    val symbol: String,
    val price: Double,
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

@Serializable
data class AssetQuote(
    val symbol: String,
    val name: String? = null,
    val price: Double,
    val changesPercentage: Double,
    val change: Double,
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
data class ChartPoint(
    val date: String,
    val open: Double,
    val low: Double,
    val high: Double,
    val close: Double,
    val volume: Long
)
