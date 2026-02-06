package com.example.visualmoney.newAsset.state

import com.example.visualmoney.AssetCategory
import com.example.visualmoney.SearchResultRowUi
import com.example.visualmoney.calendar.now
import kotlinx.datetime.LocalDate

data class AssetInputState(
    val query: String = "",
    val results: List<SearchResultRowUi> = emptyList(),
    val currentTab: AssetCategory = AssetCategory.STOCKS,
    val selectedSecurity: SearchResultRowUi? = null,
    val assetName:String = "",
    val purchasePrice: Double? = null,
    val currentValue: Double? = null,
    val purchasedAt: LocalDate = LocalDate.now(),
    val quantity: Int? = null,
    val notes: String = "",
) {
    val searchBarPlaceHolder: String
        get() = when (currentTab) {
            AssetCategory.STOCKS -> "Ticker, Stock, ETF, ..."
            AssetCategory.CRYPTO -> "eg. Bitcoin"
            AssetCategory.COMMODITIES -> "eg. Gold"
            AssetCategory.OTHER -> ""
        }
    val assetFieldTitle: String
        get() = when (currentTab) {
            AssetCategory.STOCKS -> "Add security"
            AssetCategory.CRYPTO -> "Add crypto"
            AssetCategory.COMMODITIES -> "Add commodity"
            AssetCategory.OTHER -> ""
        }
}

val AssetInputState.totalValue: Double
    get() {
        return (purchasePrice ?: 0.0) * (quantity ?: 0)
    }

val AssetInputState.isValidForSubmit: Boolean
    get() {
        return if (selectedSecurity != null) {
            (quantity ?: 0) > 0 && (purchasePrice ?: 0.0) > 0.0 && (currentValue ?: 0.0) > 0.0
        } else {
            (purchasePrice ?: 0.0) > 0.00 && (currentValue ?: 0.0) > 0.00
        }
    }

