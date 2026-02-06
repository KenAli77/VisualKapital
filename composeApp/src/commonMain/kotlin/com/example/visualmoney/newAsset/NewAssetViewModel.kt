package com.example.visualmoney.newAsset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.AssetCategory
import com.example.visualmoney.SearchResultRowUi
import com.example.visualmoney.core.toSafeDouble
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.exchangeName
import com.example.visualmoney.newAsset.event.ListedAssetInputEvent
import com.example.visualmoney.newAsset.event.ManualAssetInputEvent
import com.example.visualmoney.newAsset.state.AssetInputState
import com.example.visualmoney.newAsset.state.isValidForSubmit
import com.example.visualmoney.util.LogoUtil
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.max

// TODO: Add validation and errors
class NewAssetViewModel(private val repo: FinancialRepository) : ViewModel() {
    var listedAssetInputStateState by mutableStateOf(AssetInputState())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        searchListedAsset()
    }

    @OptIn(FlowPreview::class)
    fun searchListedAsset() {
        val priorityExchanges = setOf("NYSE", "NASDAQ", "OTC")
        viewModelScope.launch {
            snapshotFlow {
                listedAssetInputStateState.query
            }.debounce { 300 }.distinctUntilChanged().collectLatest {
                val exchange = listedAssetInputStateState.currentTab.exchangeName
                val results = repo.searchAsset(it, exchange).sortedBy { asset ->
                    if (asset.exchange in priorityExchanges) 0 else 1
                }
                isLoading = true

                listedAssetInputStateState = listedAssetInputStateState.copy(
                    results = results.map {
                        SearchResultRowUi(
                            symbol = it.symbol,
                            name = it.name,
                            priceText = it.currency,
                            assetType = AssetCategory.STOCKS,
                            exchangeName = it.exchange,
                            iconUrl = if (it.exchange == "CRYPTO") LogoUtil.getCryptoLogoUrl(it.symbol) else LogoUtil.getLogoUrl(
                                it.symbol
                            )
                        )
                    }
                )
                isLoading = false
            }
        }

    }

    fun onListedAssetInputEvent(event: ListedAssetInputEvent) {
        when (event) {
            is ListedAssetInputEvent.NameChanged -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    assetName = event.value
                )
            }

            is ListedAssetInputEvent.QueryChanged -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    query = event.query
                )
            }

            is ListedAssetInputEvent.PurchasePriceChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    purchasePrice = price
                )
            }

            is ListedAssetInputEvent.SymbolSelected -> with(event) {
                val item = listedAssetInputStateState.results.find { it.symbol == symbol }
                item?.let {
                    viewModelScope.launch {
                        val latestQuote = repo.getQuote(it.symbol)
                        latestQuote.price
                        listedAssetInputStateState = listedAssetInputStateState.copy(
                            selectedSecurity = item,
                            currentValue = latestQuote.price
                        )
                    }
                }
            }

            is ListedAssetInputEvent.SectionSelected -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    query = "",
                    currentTab = event.section
                )
            }

            is ListedAssetInputEvent.NotesChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    notes = note
                )
            }

            is ListedAssetInputEvent.PurchaseDateChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    purchasedAt = date
                )
            }

            is ListedAssetInputEvent.QtyChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    quantity = qty
                )
            }

            is ListedAssetInputEvent.Submit -> {
                if (!listedAssetInputStateState.isValidForSubmit) return
                val asset = when (listedAssetInputStateState.currentTab) {
                    AssetCategory.OTHER -> with(listedAssetInputStateState) {
                        PortfolioAsset(
                            name = assetName,
                            symbol = assetName,
                            exchangeName = AssetCategory.OTHER.exchangeName,
                            qty = quantity,
                            purchasePrice = purchasePrice,
                            purchasedAt = purchasedAt,
                            note = notes,
                            type = AssetCategory.OTHER,
                            currentPrice = currentValue,
                        )
                    }

                    else -> with(listedAssetInputStateState) {
                        selectedSecurity?.let {
                            PortfolioAsset(
                                name = it.name,
                                symbol = it.symbol,
                                exchangeName = it.exchangeName,
                                qty = quantity,
                                purchasePrice = purchasePrice,
                                purchasedAt = purchasedAt,
                                currentPrice = currentValue,
                                note = notes,
                                type = currentTab,
                            )

                        }

                    }
                }
                viewModelScope.launch {
                    asset?.let {
                        repo.addAssetToPortfolio(it)
                        listedAssetInputStateState = AssetInputState()

                    }
                }

            }
        }
    }
}