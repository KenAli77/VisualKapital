package com.example.visualmoney.data.repository

import com.example.visualmoney.data.local.FinancialEventDao
import com.example.visualmoney.data.local.FinancialEventEntity
import com.example.visualmoney.data.local.toDomain
import com.example.visualmoney.data.local.toEntity
import com.example.visualmoney.data.remote.DividendCalendarDto
import com.example.visualmoney.data.remote.EarningsCalendarDto
import com.example.visualmoney.data.remote.FmpDataSource
import com.example.visualmoney.data.remote.StockSplitCalendarDto
import com.example.visualmoney.domain.model.FinancialEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private const val EVENTS_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours

interface EventsRepository {
    /**
     * Observe financial events for given symbols within the next 30 days.
     * Automatically refreshes stale data in background.
     */
    fun observePortfolioEvents(symbols: List<String>): Flow<List<FinancialEvent>>

    /**
     * Force refresh events for given symbols from API.
     */
    suspend fun refreshEvents(symbols: List<String>)
}

class EventsRepositoryImpl(
    private val remoteSource: FmpDataSource,
    private val eventDao: FinancialEventDao
) : EventsRepository {

    override fun observePortfolioEvents(symbols: List<String>): Flow<List<FinancialEvent>> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val fromDate = today.toString()
        val toDate = today.plus(30, DateTimeUnit.DAY).toString()

        return eventDao.observeEventsForSymbols(symbols, fromDate, toDate)
            .map { entities ->
                entities.mapNotNull { it.toDomain() }
            }
    }

    override suspend fun refreshEvents(symbols: List<String>) {
        if (symbols.isEmpty()) return

        val now = Clock.System.now().toEpochMilliseconds()
        val cutoff = now - EVENTS_TTL_MS

        // Find which symbols have stale or missing data
        val cachedSymbols = eventDao.getCachedSymbols(symbols).toSet()
        val staleSymbols = eventDao.getStaleSymbols(symbols, cutoff).toSet()

        val needsRefresh = symbols.filter { it !in cachedSymbols || it in staleSymbols }

        if (needsRefresh.isEmpty()) {
            println("EventsRepository: All symbols are fresh, skipping API call")
            return
        }

        println("EventsRepository: Refreshing events for ${needsRefresh.size} symbols: $needsRefresh")

        // Fetch 30-day window
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val fromDate = today.toString()
        val toDate = today.plus(30, DateTimeUnit.DAY).toString()

        // Fetch all calendar data in parallel (3 API calls)
        val earnings = remoteSource.getEarningsCalendar(fromDate, toDate)
        val dividends = remoteSource.getDividendCalendar(fromDate, toDate)
        val splits = remoteSource.getStockSplitCalendar(fromDate, toDate)

        println("EventsRepository: Fetched ${earnings.size} earnings, ${dividends.size} dividends, ${splits.size} splits from API")

        // Filter to only portfolio symbols
        val symbolSet = needsRefresh.map { it.uppercase() }.toSet()

        val earningEvents = earnings
            .filter { it.symbol?.uppercase() in symbolSet }
            .mapNotNull { it.toDomain() }

        val dividendEvents = dividends
            .filter { it.symbol?.uppercase() in symbolSet }
            .mapNotNull { it.toDomain() }

        val splitEvents = splits
            .filter { it.symbol?.uppercase() in symbolSet }
            .mapNotNull { it.toDomain() }

        val allEvents = earningEvents + dividendEvents + splitEvents
        println("EventsRepository: Filtered to ${allEvents.size} events for portfolio symbols")

        // Convert to entities and save
        val entities = allEvents.map { it.toEntity(now) }
        eventDao.upsertAll(entities)

        // Cleanup old stale data periodically
        val olderCutoff = now - (7 * EVENTS_TTL_MS) // 7 days old
        eventDao.deleteStale(olderCutoff)
    }
}

// -------- DTO to Domain mapping --------

private fun EarningsCalendarDto.toDomain(): FinancialEvent.Earnings? {
    val sym = symbol ?: return null
    val d = date ?: return null
    val localDate = try {
        LocalDate.parse(d)
    } catch (e: Exception) {
        return null
    }

    return FinancialEvent.Earnings(
        symbol = sym,
        date = localDate,
        epsEstimated = epsEstimated,
        epsActual = eps,
        revenueEstimated = revenueEstimated,
        revenueActual = revenue,
        time = time
    )
}

private fun DividendCalendarDto.toDomain(): FinancialEvent.Dividend? {
    val sym = symbol ?: return null
    val d = date ?: return null
    val localDate = try {
        LocalDate.parse(d)
    } catch (e: Exception) {
        return null
    }

    return FinancialEvent.Dividend(
        symbol = sym,
        date = localDate,
        paymentDate = paymentDate?.let { LocalDate.parse(it) },
        recordDate = recordDate?.let { LocalDate.parse(it) },
        declarationDate = declarationDate?.let { LocalDate.parse(it) },
        amount = dividend ?: 0.0,
        yield = null
    )
}

private fun StockSplitCalendarDto.toDomain(): FinancialEvent.StockSplit? {
    val sym = symbol ?: return null
    val d = date ?: return null
    val localDate = try {
        LocalDate.parse(d)
    } catch (e: Exception) {
        return null
    }

    return FinancialEvent.StockSplit(
        symbol = sym,
        date = localDate,
        numerator = numerator ?: 1.0,
        denominator = denominator ?: 1.0
    )
}
