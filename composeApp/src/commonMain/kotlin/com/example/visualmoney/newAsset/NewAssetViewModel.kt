package com.example.visualmoney.newAsset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.ExploreTab
import com.example.visualmoney.SearchResultRowUi
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.newAsset.event.ListedAssetInputEvent
import com.example.visualmoney.newAsset.event.ManualAssetInputEvent
import com.example.visualmoney.newAsset.state.ListedAssetInputState
import com.example.visualmoney.util.LogoUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.max

class NewAssetViewModel(private val repo: FinancialRepository) : ViewModel() {
    var manualAssetInputState by mutableStateOf(ManualAssetInputState())
        private set

    var listedAssetInputState by mutableStateOf(ListedAssetInputState())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        searchListedAsset()
    }

    fun searchListedAsset() {
        viewModelScope.launch {
            snapshotFlow {
                listedAssetInputState.query
            }.debounce { 300 }.distinctUntilChanged().collectLatest {
                val results = repo.searchAsset(it)
                isLoading = true
                listedAssetInputState = listedAssetInputState.copy(
                    results = results.map {
                        SearchResultRowUi(
                            symbol = it.symbol,
                            name = it.name,
                            priceText = it.currency,
                            assetType = ExploreTab.STOCKS,
                            exchangeName = it.exchange,
                            iconUrl = LogoUtil.getLogoUrl(it.symbol)
                        )
                    }
                )
                isLoading = false
            }
        }

    }

    fun onListedAssetInputEvent(event: ListedAssetInputEvent) {
        when (event) {
            is ListedAssetInputEvent.QueryChanged -> {
                listedAssetInputState = listedAssetInputState.copy(
                    query = event.query
                )

            }

            is ListedAssetInputEvent.SymbolSelected -> {

            }

            is ListedAssetInputEvent.SectionSelected -> {
                listedAssetInputState = listedAssetInputState.copy(
                    query = "",
                    currentTab = event.section
                )
            }
        }
    }

    fun onFixedAssetInputEvent(event: ManualAssetInputEvent) {
        when (event) {
            is ManualAssetInputEvent.NameChanged -> {
                manualAssetInputState = manualAssetInputState.copy(name = event.value)
                recalc()
            }

            is ManualAssetInputEvent.QuantityChanged -> {
                // allow empty while typing; sanitize later
                manualAssetInputState = manualAssetInputState.copy(quantityText = event.value)
                recalc()
            }

            is ManualAssetInputEvent.UnitPriceChanged -> {
                manualAssetInputState = manualAssetInputState.copy(unitPriceText = event.value)
                recalc()
            }

            is ManualAssetInputEvent.PurchaseDateChanged -> {
                manualAssetInputState = manualAssetInputState.copy(purchaseDate = event.value)
                recalc()
            }

            is ManualAssetInputEvent.CountryChanged -> {
                manualAssetInputState = manualAssetInputState.copy(country = event.value)
                recalc()
            }

            is ManualAssetInputEvent.SectorChanged -> {
                manualAssetInputState = manualAssetInputState.copy(sector = event.value)
                recalc()
            }


            ManualAssetInputEvent.Submit -> {
                // MVP: just validate and you’d persist/create the asset
                val s = manualAssetInputState
                if (!s.canSubmit) {
                    manualAssetInputState =
                        s.copy(error = s.error ?: "Please complete required fields.")
                    return
                }

                val qty = parseQuantity(s.quantityText)
                val unitPrice = parseMoney(s.unitPriceText)

                // TODO: Persist your asset (repository call)
                // createManualAsset(name=s.name, quantity=qty, unitPrice=unitPrice, date=s.purchaseDate, ...)

                // Optional: reset
                manualAssetInputState = ManualAssetInputState()
            }
        }

    }

    private fun recalc() {
        val s = manualAssetInputState

        val nameOk = s.name.trim().isNotEmpty()
        val qty = parseQuantity(s.quantityText)
        val price = parseMoney(s.unitPriceText)

        val qtyOk = qty > 0
        val priceOk = price > 0.0

        val total = qty * price

        val error = when {
            !nameOk -> "Name is required."
            !qtyOk -> "Quantity must be at least 1."
            !priceOk -> "Enter a valid price."
            else -> null
        }

        manualAssetInputState = s.copy(
            computedTotalValue = total,
            canSubmit = (error == null),
            error = null // clear error as user edits; keep only on submit if you want
        )
    }

    private fun parseQuantity(text: String): Int {
        // Treat empty as 0 while typing
        val n = text.trim().toIntOrNull() ?: 0
        return max(0, n)
    }

    private fun parseMoney(text: String): Double {
        // Basic parser: supports "12.34" and also "12,34"
        val normalized = text.trim().replace(',', '.')
        return normalized.toDoubleOrNull() ?: 0.0
    }
}