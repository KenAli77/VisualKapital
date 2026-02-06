package com.example.visualmoney.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.util.LogoUtil
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Fetching Top Gainers as the default list for now
               repository.getPortfolioAssets().collect { assets ->

                val holdings = assets.map { asset ->

                    val assetClass = AssetClass.STOCK
                    val quote = repository.getQuote(asset.symbol)
                    val changePercentage = if (asset.purchasePrice != 0.0) {
                        ((quote.price - asset.purchasePrice) / asset.purchasePrice) * 100
                    } else {
                        0.0
                    }

                    HoldingRowUi(
                        symbol = asset.symbol,
                        name = asset.name,
                        assetClass = assetClass,
                        changePct = changePercentage,
                        price = quote.price,
                        dayLow = quote.dayLow ?: quote.price,
                        dayHigh = quote.dayHigh ?: quote.price,
                        logoUrl = LogoUtil.getLogoUrl(quote.symbol)
                    )
                }

                state = state.copy(
                    holdings = holdings,
                    isLoading = false
                )
               }
            } catch (e: Exception) {
                // Handle error
                state = state.copy(isLoading = false)
                println("Error loading home data: ${e}")
            }
        }
    }
}

data class HomeUiState(
    val holdings: List<HoldingRowUi> = emptyList(),
    val isLoading: Boolean = true
)
