package com.example.visualmoney.newAsset.event

import com.example.visualmoney.AssetCategory
import kotlinx.datetime.LocalDate

sealed interface ListedAssetInputEvent {
    data class NameChanged(val value: String) : ListedAssetInputEvent
    data class QueryChanged(val query: String) : ListedAssetInputEvent
    data class SymbolSelected(val symbol: String) : ListedAssetInputEvent
    data class SectionSelected(val section: AssetCategory) : ListedAssetInputEvent
    data class PurchasePriceChanged(val price: Double) : ListedAssetInputEvent
    data class PurchaseDateChanged(val date: LocalDate) : ListedAssetInputEvent

    data class QtyChanged(val qty: Int) : ListedAssetInputEvent

    data class NotesChanged(val note:String): ListedAssetInputEvent

    data object Submit: ListedAssetInputEvent
}