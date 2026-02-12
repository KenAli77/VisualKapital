package com.visualmoney.app.calendar

import androidx.compose.ui.graphics.Color
import com.visualmoney.app.DefaultAppColors
import com.visualmoney.app.data.local.InvestmentReminderEntity
import com.visualmoney.app.data.local.PortfolioAsset
import kotlinx.datetime.LocalDate

data class CalendarState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedAsset: PortfolioAsset? = null,
    val availableAssets: List<PortfolioAsset> = emptyList(),
    val reminders: List<InvestmentReminderEntity> = emptyList(),
    val reminderInputVisible: Boolean = false,
)

val CalendarState.remindersForSelected: List<ReminderUi>
    get() {
        return reminders.filter { it.dueDate == selectedDate }.mapNotNull { reminder ->
            val asset = availableAssets.find { it.symbol == reminder.symbol }
            asset?.let {
                reminder.toReminderUI(it)
            }
        }
    }
val CalendarState.remindersUI: List<ReminderUi>
    get() = reminders.mapNotNull { reminder ->
        val asset = availableAssets.find { it.symbol == reminder.symbol }
        asset?.let {
            reminder.toReminderUI(it)
        }
    }

val CalendarState.showRemindersBadge: Boolean get() = reminders.any { !it.isDone }
val CalendarState.reminderBadgeColor: Color
    get() {
        val today = reminders.any { !it.isDone && it.dueDate <= LocalDate.now() }
        val others = reminders.any { it.isDone }
        return if (today) DefaultAppColors.error else if (others) DefaultAppColors.warning else Color.Transparent
    }