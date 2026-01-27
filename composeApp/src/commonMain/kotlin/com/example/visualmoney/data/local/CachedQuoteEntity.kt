package com.example.visualmoney.data.local


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.visualmoney.domain.model.AssetQuote
import kotlin.time.Clock

@Entity(
    tableName = "cached_quotes",
    indices = [
        Index(value = ["symbol"], unique = true),
        Index(value = ["updatedAtEpochMs"])
    ]
)
data class CachedQuoteEntity(
    @PrimaryKey val symbol: String,          // e.g. "AAPL", "BTCUSD", "XAUUSD"
    val price: Double,
    val changePct: Double? = null,
    val change: Double? = null,
    val dayLow: Double? = null,
    val dayHigh: Double? = null,
    val yearLow: Double? = null,
    val yearHigh: Double? = null,
    val volume: Long? = null,
    val marketCap: Double? = null,
    val currency: String? = null,
    val updatedAtEpochMs: Long = Clock.System.now().toEpochMilliseconds(),
    val exchange: String? = null,
    val eps: Double? = null,
    val pe: Double? = null,
    val earningsAnnouncement: String? = null,
    val sharesOutstanding: Long? = null,
    )

fun CachedQuoteEntity.toAsset():AssetQuote = AssetQuote(
    symbol = symbol,
    price = price,
    changesPercentage = changePct ?: 0.0,
    change = change ?: 0.0,
    dayLow = dayLow,
    dayHigh = dayHigh,
    yearLow = yearLow,
    yearHigh = yearHigh,
    volume = volume,
    marketCap = marketCap,
    timestamp = updatedAtEpochMs,
    exchange = exchange,
    eps = eps,
    pe = pe,
    earningsAnnouncement = earningsAnnouncement,
    sharesOutstanding = sharesOutstanding
)




