package com.example.visualmoney.calendar

import kotlinx.datetime.LocalDate

sealed interface ReminderInputEvent {
    data class NoteChanged(val note:String) : ReminderInputEvent
    data class SymbolChanged(val symbol:String) : ReminderInputEvent
    data class DateChanged(val date: LocalDate) : ReminderInputEvent
}