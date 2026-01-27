package com.example.visualmoney.data.repository

import com.example.visualmoney.data.local.CachedQuoteDao
import com.example.visualmoney.data.local.CachedQuoteEntity
import com.example.visualmoney.data.local.toAsset
import com.example.visualmoney.data.remote.FmpDataSource
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock

private const val QUOTE_TTL_MS = 5 * 60 * 60 * 1000L // 5 hours

interface FinancialRepository {
    suspend fun getQuote(symbol: String): AssetQuote
    suspend fun getQuotes(symbols: List<String>): List<AssetQuote>
    suspend fun getProfile(symbol: String): AssetProfile
    suspend fun getTopGainers(): List<AssetQuote>
    suspend fun getTopLosers(): List<AssetQuote>
    suspend fun getCommodities(): List<AssetQuote>
    suspend fun getChart(symbol: String): List<ChartPoint>
}

class FinancialRepositoryImpl(
    private val remoteSource: FmpDataSource,
    private val cachedQuoteDao: CachedQuoteDao

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
            changePct = remote.changesPercentage,
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

    override suspend fun getTopGainers(): List<AssetQuote> {
        val cache = cachedQuoteDao.observeAll().firstOrNull()
        if (!cache.isNullOrEmpty()) {
            return cache.map { it.toAsset() }
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val remote = remoteSource.getTopGainers()
            cachedQuoteDao.upsertAll(remote.map { it.toCachedQuoteEntity(updatedAt = now) })
            return remote
        }
    }

    override suspend fun getTopLosers(): List<AssetQuote> = remoteSource.getTopLosers()

    override suspend fun getCommodities(): List<AssetQuote> = remoteSource.getCommodities()
    override suspend fun getChart(symbol: String): List<ChartPoint> = remoteSource.getChart(symbol)

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
                changePct = q.changesPercentage,
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
        changePct = changesPercentage,
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
