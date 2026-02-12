package com.visualmoney.app.calendar

import kotlinx.datetime.LocalDate

sealed interface CalendarEvent {
    data class DateSelected(val date: LocalDate) : CalendarEvent
    data object PrevMonth : CalendarEvent
    data object NextMonth : CalendarEvent
    data class OnRemoveReminder(val id: String) : CalendarEvent
    data class OnReminderDoneChanged(val id: String, val done: Boolean) : CalendarEvent

}