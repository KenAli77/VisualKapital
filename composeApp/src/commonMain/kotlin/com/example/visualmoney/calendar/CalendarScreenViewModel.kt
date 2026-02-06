package com.example.visualmoney.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CalendarScreenViewModel : ViewModel() {

    var newReminderState by mutableStateOf(ReminderInputState())
        private set
    var state by mutableStateOf(CalendarState())

    fun onEvent(event: ReminderInputEvent) {
        when (event) {
            is ReminderInputEvent.DateChanged -> {
                newReminderState = newReminderState.copy(date = event.date)
            }

            is ReminderInputEvent.NoteChanged -> {
                newReminderState = newReminderState.copy(notes = event.note)
            }

            is ReminderInputEvent.SymbolChanged -> {
                val asset = state.availableAssets.find { it.symbol == event.symbol }
                newReminderState = newReminderState.copy(selectedAsset = asset)
            }
        }
    }
}