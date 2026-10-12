package com.visualmoney.app.data.repository

import com.visualmoney.app.calendar.now
import com.visualmoney.app.core.toLocalDateTime
import com.visualmoney.app.data.local.CachedQuoteDao
import com.visualmoney.app.data.local.CachedQuoteEntity
import com.visualmoney.app.data.local.PortfolioAsset
import com.visualmoney.app.data.local.PortfolioAssetDAO
import com.visualmoney.app.data.local.SearchResult
import com.visualmoney.app.data.local.toAsset
import com.visualmoney.app.data.remote.FmpDataSource

import com.visualmoney.app.domain.model.AssetProfile
import com.visualmoney.app.domain.model.AssetQuote
import com.visualmoney.app.domain.model.ChartPoint
import com.visualmoney.app.domain.model.Dividend
import com.visualmoney.app.domain.model.SplitEvent
import com.visualmoney.app.domain.model.StockNews
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

private const val QUOTE_TTL_MS = 5 * 60 * 60 * 1000L // 5 hours

interface FinancialRepository {
    suspend fun getQuote(symbol: String): AssetQuote
    suspend fun getQuotes(symbols: List<String>): List<AssetQuote>
    suspend fun getProfile(symbol: String): AssetProfile
    suspend fun getTopGainers(): List<AssetQuote>
    suspend fun getTopLosers(): List<AssetQuote>
    suspend fun getCommodities(): List<AssetQuote>
    suspend fun getChart(symbol: String,from: String,to: String): List<ChartPoint>
    suspend fun searchAsset(name:String,exchange: String):List<SearchResult>
    suspend fun loadEtfs():List<SearchResult>
    suspend fun loadCryptos():List<SearchResult>
    suspend fun loadCommodities():List<SearchResult>
    suspend fun addAssetToPortfolio(asset: PortfolioAsset)
    suspend fun getPortfolioAssets(): Flow<List<PortfolioAsset>>
    suspend fun getPortfolioAsset(symbol:String): Flow<PortfolioAsset?>
    suspend fun getStockNews(symbols: List<String>): List<StockNews>
    suspend fun getCryptoNews(symbols: List<String>): List<StockNews>
    suspend fun getProfiles(symbols: List<String>): List<AssetProfile>
    suspend fun getDividends(symbol: String): List<Dividend>
    suspend fun getSplits(symbol: String): List<SplitEvent>
}

class FinancialRepositoryImpl(
    private val remoteSource: FmpDataSource,
    private val cachedQuoteDao: CachedQuoteDao,
    private val portfolioDao: PortfolioAssetDAO

) : FinancialRepository {
    override suspend fun addAssetToPortfolio(asset: PortfolioAsset) {
        portfolioDao.upsert(asset)
    }

    override suspend fun getPortfolioAssets(): Flow<List<PortfolioAsset>> {
       return portfolioDao.observeAllAssets()
    }

    override suspend fun getPortfolioAsset(symbol: String): Flow<PortfolioAsset?> {
        return portfolioDao.observeAsset(symbol)
    }

    override suspend fun getStockNews(symbols: List<String>): List<StockNews> {
        return remoteSource.getStockNews(symbols)
    }

    override suspend fun getCryptoNews(symbols: List<String>): List<StockNews> {
        return remoteSource.getCryptoNews(symbols)
    }

    override suspend fun getProfiles(symbols: List<String>): List<AssetProfile> {
        return remoteSource.getProfiles(symbols)
    }


    override suspend fun getQuote(symbol: String): AssetQuote {
        val now = Clock.System.now().toEpochMilliseconds()
        val local = cachedQuoteDao.get(symbol)
        local?.let {
            if (it.updatedAtEpochMs.toLocalDateTime().date == LocalDate.now()) {
                return@getQuote it.toAsset()
            }
        }
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
    override suspend fun getChart(symbol: String, from: String, to: String): List<ChartPoint> = remoteSource.getChart(symbol, from = from, to = to)
    override suspend fun searchAsset(name: String, exchange:String): List<SearchResult> {
        val remote =  remoteSource.searchCompanyByName(name,exchange)
        return remote
    }

    override suspend fun loadEtfs(): List<SearchResult> {
        TODO("Not yet implemented")
    }

    override suspend fun loadCryptos(): List<SearchResult> {
        val remote = remoteSource.getCrypto()
//        cachedQuoteDao.upsertAll(remote.map {
//            CachedQuoteEntity(
//                symbol = it.symbol,
//                exchange = it.exchange,
//            )
//        })
        return remote
    }

    override suspend fun loadCommodities(): List<SearchResult> {
        val remote = remoteSource.getCommodities()

        return emptyList()
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

    override suspend fun getDividends(symbol: String): List<Dividend> {
        return remoteSource.getDividends(symbol)
    }

    override suspend fun getSplits(symbol: String): List<SplitEvent> {
        return remoteSource.getSplits(symbol)
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
