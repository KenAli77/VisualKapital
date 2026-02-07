package com.example.visualmoney.calendar

import com.example.visualmoney.data.local.InvestmentReminderEntity
import com.example.visualmoney.data.local.PortfolioAsset
import kotlinx.datetime.LocalDate

data class CalendarState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedAsset: PortfolioAsset? = null,
    val availableAssets:List<PortfolioAsset> = emptyList(),
    val reminders:List<InvestmentReminderEntity> = emptyList(),
    val reminderInputVisible: Boolean = false,
)
