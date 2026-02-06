package com.example.visualmoney.calendar

import com.example.visualmoney.data.local.PortfolioAsset
import kotlinx.datetime.LocalDate

data class ReminderInputState(
    val date: LocalDate = LocalDate.now(),
    val selectedAsset: PortfolioAsset? = null,
    val notes:String = ""
)
