package com.example.visualmoney.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.LoadingManager
import com.example.visualmoney.SnackbarManager
import com.example.visualmoney.SnackbarType
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.data.repository.InvestmentReminderRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

class CalendarScreenViewModel(
    private val repo: FinancialRepository,
    private val remindersRepo: InvestmentReminderRepository
) : ViewModel() {
    var newReminderState by mutableStateOf(ReminderInputState())
        private set
    var state by mutableStateOf(CalendarState())

    init {
        viewModelScope.launch {
            repo.getPortfolioAssets().collect {
                newReminderState = newReminderState.copy(
                    assets = it,
                    searchResults = it
                )
                state = state.copy(
                    availableAssets = it
                )
            }
        }
        viewModelScope.launch {
            remindersRepo.observeAll().collect {
                state = state.copy(
                    reminders = it
                )
                var showRemindersBadge = it.any { !it.isDone && it.dueDate <= LocalDate.now() }

            }
        }
    }


    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.DateSelected -> {
                state = state.copy(
                    selectedDate = event.date
                )
            }

            is CalendarEvent.OnReminderDoneChanged -> {
                viewModelScope.launch {
                    remindersRepo.setDone(event.id, event.done)
                }
            }

            is CalendarEvent.OnRemoveReminder -> {
                viewModelScope.launch {
                    remindersRepo.delete(event.id)
                    SnackbarManager.showMessage("Reminder deleted", SnackbarType.SUCCESS)
                }
            }

            is CalendarEvent.NextMonth -> {
                val nextMonth = state.selectedDate.yearMonth.plusMonths(1)
                val date = nextMonth.atDay(1)
                state = state.copy(
                    selectedDate = date
                )
            }

            is CalendarEvent.PrevMonth -> {
                val prevMonth = state.selectedDate.yearMonth.plusMonths(1)
                val date = prevMonth.atDay(1)
                state = state.copy(
                    selectedDate = date
                )

            }
        }
    }

    fun onEvent(event: ReminderInputEvent) {
        when (event) {
            is ReminderInputEvent.DateChanged -> {
                newReminderState = newReminderState.copy(date = event.date)
            }

            is ReminderInputEvent.QueryChanged -> {
                val results =
                    if (event.query.isBlank()) newReminderState.assets else newReminderState.assets.asSequence()
                        .filter {
                            it.symbol.contains(event.query, ignoreCase = true) || it.name.contains(
                                event.query,
                                ignoreCase = true
                            )
                        }.sortedBy { it.symbol }.toList()

                newReminderState = newReminderState.copy(
                    query = event.query,
                    searchResults = results
                )
            }

            is ReminderInputEvent.DescriptionChanged -> {
                newReminderState = newReminderState.copy(description = event.description)
            }

            is ReminderInputEvent.HideReminderInputSheet -> {
                state = state.copy(reminderInputVisible = false)
            }

            is ReminderInputEvent.ShowReminderInputSheet -> {
                state = state.copy(reminderInputVisible = true)

            }

            is ReminderInputEvent.Save -> {
                if (newReminderState.isValidForSubmit) {
                    println("Ready for save of reminder with asset: ${newReminderState.selectedAsset}")
                    viewModelScope.launch {
                        LoadingManager.startLoading()
                        val reminder = newReminderState.toReminder()
                        remindersRepo.upsert(reminder)
                        LoadingManager.stopLoading()
                        newReminderState = ReminderInputState()
                        state = state.copy(
                            reminderInputVisible = false
                        )
                        SnackbarManager.showMessage("Reminder saved", SnackbarType.SUCCESS)

                    }
                } else {
                    SnackbarManager.showMessage(
                        "Please enter all required fields",
                        SnackbarType.ERROR
                    )
                }
            }

            is ReminderInputEvent.NoteChanged -> {
                newReminderState = newReminderState.copy(notes = event.note)
            }

            is ReminderInputEvent.SymbolChanged -> {
                val asset = newReminderState.assets.find { it.symbol == event.symbol }
                newReminderState = newReminderState.copy(selectedAsset = asset)
            }
        }
    }
}