package com.example.visualmoney.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.local.PortfolioBuyDao
import com.example.visualmoney.data.repository.EventsRepository
import com.example.visualmoney.domain.model.EventType
import com.example.visualmoney.domain.model.FinancialEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CalendarViewModel(
    private val portfolioBuyDao: PortfolioBuyDao,
    private val eventsRepository: EventsRepository
) : ViewModel() {

    var state by mutableStateOf(CalendarUiState())
        private set

    init {
        observePortfolioAndEvents()
    }

    private fun observePortfolioAndEvents() {
        viewModelScope.launch {
            // First, get portfolio symbols
            portfolioBuyDao.observePositions()
                .map { positions -> positions.map { it.symbol }.distinct() }
                .distinctUntilChanged()
                .collectLatest { symbols ->
                    if (symbols.isEmpty()) {
                        state = state.copy(events = emptyList(), isLoading = false)
                        return@collectLatest
                    }

                    // Refresh stale events in background
                    launch {
                        try {
                            eventsRepository.refreshEvents(symbols)
                        } catch (e: Exception) {
                            println("CalendarViewModel: Error refreshing events: ${e.message}")
                        }
                    }

                    // Observe events from cache
                    eventsRepository.observePortfolioEvents(symbols)
                        .collectLatest { events ->
                            val reminders = events.map { it.toReminderUi() }
                            state = state.copy(
                                events = events,
                                reminders = reminders,
                                isLoading = false
                            )
                        }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val symbols = portfolioBuyDao.observePositions()
                    .map { positions -> positions.map { it.symbol }.distinct() }
                // Force refresh from API
                // This could be enhanced with a forceRefresh parameter
            } catch (e: Exception) {
                println("CalendarViewModel: Error during refresh: ${e.message}")
            }
            state = state.copy(isLoading = false)
        }
    }
}

data class CalendarUiState(
    val events: List<FinancialEvent> = emptyList(),
    val reminders: List<ReminderUi> = emptyList(),
    val isLoading: Boolean = true
)

// -------- Event to ReminderUi mapping --------

private fun FinancialEvent.toReminderUi(): ReminderUi {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val status = when {
        date < today -> ReminderStatus.MISSED
        else -> ReminderStatus.UPCOMING
    }

    return when (this) {
        is FinancialEvent.Earnings -> ReminderUi(
            id = "earnings_${symbol}_$date",
            title = "$symbol Earnings",
            subtitle = when (time) {
                "bmo" -> "Before Market Open"
                "amc" -> "After Market Close"
                else -> "Earnings Report"
            },
            date = date,
            timeLabel = when (time) {
                "bmo" -> "Pre-market"
                "amc" -> "After-hours"
                else -> "TBD"
            },
            amountLabel = epsEstimated?.let { "EPS Est: $${"%.2f".format(it)}" },
            status = status,
            eventType = EventType.EARNINGS
        )
        is FinancialEvent.Dividend -> ReminderUi(
            id = "dividend_${symbol}_$date",
            title = "$symbol Dividend",
            subtitle = "Ex-Dividend Date",
            date = date,
            timeLabel = paymentDate?.let { "Pays ${it.month.name.take(3)} ${it.dayOfMonth}" } ?: "Payment TBD",
            amountLabel = "$${"%.4f".format(amount)}",
            status = status,
            eventType = EventType.DIVIDEND
        )
        is FinancialEvent.StockSplit -> ReminderUi(
            id = "split_${symbol}_$date",
            title = "$symbol Stock Split",
            subtitle = "$ratio split",
            date = date,
            timeLabel = "Market Open",
            amountLabel = null,
            status = status,
            eventType = EventType.STOCK_SPLIT
        )
    }
}
