package com.visualmoney.app.calendar

import com.visualmoney.app.data.local.InvestmentReminderEntity
import com.visualmoney.app.data.local.PortfolioAsset
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ReminderInputState(
    val date: LocalDate = LocalDate.now(),
    val selectedAsset: PortfolioAsset? = null,
    val assets:List<PortfolioAsset> = emptyList(),
    val query:String = "",
    val searchResults:List<PortfolioAsset> = emptyList(),
    val description:String = "",
    val notes:String = ""
)

val ReminderInputState.isValidForSubmit: Boolean get() {
    return selectedAsset != null && description.isNotBlank()
}

@OptIn(ExperimentalUuidApi::class)
fun ReminderInputState.toReminder(): InvestmentReminderEntity {
    return InvestmentReminderEntity(
        id = Uuid.random().toString(),
        symbol = selectedAsset!!.symbol,
        dueDate = date,
        description = description,
        note = notes
    )
}