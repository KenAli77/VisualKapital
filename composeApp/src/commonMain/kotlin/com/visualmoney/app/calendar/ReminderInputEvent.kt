package com.visualmoney.app.calendar

import kotlinx.datetime.LocalDate

sealed interface ReminderInputEvent {
    data class NoteChanged(val note: String) : ReminderInputEvent
    data class DescriptionChanged(val description: String) : ReminderInputEvent
    data class SymbolChanged(val symbol: String) : ReminderInputEvent
    data class DateChanged(val date: LocalDate) : ReminderInputEvent
    data class QueryChanged(val query: String) : ReminderInputEvent
    data object ShowReminderInputSheet: ReminderInputEvent
    data object HideReminderInputSheet: ReminderInputEvent
    data object Save : ReminderInputEvent
}