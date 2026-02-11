package com.example.visualmoney.newAsset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.AssetCategory
import com.example.visualmoney.LoadingManager
import com.example.visualmoney.SearchResultRowUi
import com.example.visualmoney.SnackbarManager
import com.example.visualmoney.SnackbarType
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.exchangeName
import com.example.visualmoney.newAsset.event.AssetInputEvent
import com.example.visualmoney.newAsset.state.AssetInputState
import com.example.visualmoney.newAsset.state.isValidForSubmit
import com.example.visualmoney.util.LogoUtil
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.example.visualmoney.data.repository.InvestmentReminderRepository
import com.example.visualmoney.data.local.InvestmentReminderEntity
import kotlinx.datetime.LocalDate
import com.example.visualmoney.calendar.now
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


// TODO: Add validation and errors
class NewAssetViewModel(
    private val repo: FinancialRepository,
    private val remindersRepo: InvestmentReminderRepository
    ) : ViewModel() {
    var listedAssetInputStateState by mutableStateOf(AssetInputState())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        searchListedAsset()
    }
    private val _events = Channel<Boolean>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    @OptIn(ExperimentalUuidApi::class)
    fun onListedAssetInputEvent(event: AssetInputEvent) {
        when (event) {
            is AssetInputEvent.NameChanged -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    assetName = event.value
                )
            }

            is AssetInputEvent.CurrentValueChanged -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    currentValue = event.price
                )
            }

            is AssetInputEvent.QueryChanged -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    query = event.query
                )
            }

            is AssetInputEvent.PurchasePriceChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    purchasePrice = price
                )
            }

            is AssetInputEvent.SymbolSelected -> with(event) {
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

            is AssetInputEvent.SectionSelected -> {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    query = "",
                    currentTab = event.section
                )
            }

            is AssetInputEvent.NotesChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    notes = note
                )
            }

            is AssetInputEvent.PurchaseDateChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    purchasedAt = date
                )
            }

            is AssetInputEvent.QtyChanged -> with(event) {
                listedAssetInputStateState = listedAssetInputStateState.copy(
                    quantity = qty
                )
            }

            is AssetInputEvent.Submit -> {
                if (!listedAssetInputStateState.isValidForSubmit) {
                    SnackbarManager.showMessage(
                        "Please enter all required fields",
                        SnackbarType.ERROR
                    )
                    return
                }
                LoadingManager.startLoading()
                val asset = when (listedAssetInputStateState.currentTab) {
                    AssetCategory.OTHER -> with(listedAssetInputStateState) {
                        PortfolioAsset(
                            name = assetName,
                            symbol = assetName,
                            exchangeName = AssetCategory.OTHER.exchangeName,
                            qty = quantity ?: 0,
                            purchasePrice = purchasePrice ?: 0.0,
                            purchasedAt = purchasedAt,
                            note = notes,
                            type = AssetCategory.OTHER,
                            currentPrice = currentValue ?: 0.0,
                        )
                    }

                    else -> with(listedAssetInputStateState) {
                        selectedSecurity?.let {
                            PortfolioAsset(
                                name = it.name,
                                symbol = it.symbol,
                                exchangeName = it.exchangeName,
                                qty = quantity ?: 0,
                                purchasePrice = purchasePrice ?: 0.0,
                                purchasedAt = purchasedAt,
                                currentPrice = currentValue ?: 0.0,
                                note = notes,
                                type = currentTab,
                            )
                        }
                    }
                }
                viewModelScope.launch {
                    asset?.let {
                        repo.addAssetToPortfolio(it)
                        
                        // Fetch and schedule dividends
                        try {
                            if (it.type == AssetCategory.STOCKS) {
                                val dividends = repo.getDividends(it.symbol)
                                val today = LocalDate.now()
                                
                                dividends.filter { dividend ->
                                    try {
                                        val exDate = LocalDate.parse(dividend.date)
                                        exDate >= today
                                    } catch (e: Exception) {
                                        false
                                    }
                                }.forEach { dividend ->
                                    val exDate = LocalDate.parse(dividend.date)
                                    val reminder = InvestmentReminderEntity(
                                        id = Uuid.random().toString(),
                                        symbol = it.symbol,
                                        description = "Dividend: ${it.symbol}",
                                        note = "Amount: ${dividend.dividend}\nPayment Date: ${dividend.paymentDate}",
                                        dueDate = exDate,
                                        isDone = false
                                    )
                                    remindersRepo.upsert(reminder)
                                }
                            }
                        } catch (e: Exception) {
                            println("Error scheduling dividends: ${e.message}")
                        }

                        listedAssetInputStateState = AssetInputState()
                        LoadingManager.stopLoading()
                        SnackbarManager.showMessage(
                            "Asset added to portfolio 🎉",
                            SnackbarType.SUCCESS
                        )
                        _events.send(true)
                    }
                }

            }
        }
    }
}
