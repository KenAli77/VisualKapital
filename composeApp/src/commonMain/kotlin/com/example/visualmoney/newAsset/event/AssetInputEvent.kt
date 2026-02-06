package com.example.visualmoney.newAsset.event

import com.example.visualmoney.AssetCategory
import kotlinx.datetime.LocalDate

sealed interface AssetInputEvent {
    data class NameChanged(val value: String) : AssetInputEvent
    data class QueryChanged(val query: String) : AssetInputEvent
    data class SymbolSelected(val symbol: String) : AssetInputEvent
    data class SectionSelected(val section: AssetCategory) : AssetInputEvent
    data class PurchasePriceChanged(val price: Double?) : AssetInputEvent
    data class CurrentValueChanged(val price: Double?) : AssetInputEvent
    data class PurchaseDateChanged(val date: LocalDate) : AssetInputEvent

    data class QtyChanged(val qty: Int?) : AssetInputEvent

    data class NotesChanged(val note: String) : AssetInputEvent

    data object Submit : AssetInputEvent
}