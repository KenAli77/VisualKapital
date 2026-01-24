package com.example.visualmoney.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.util.LogoUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Fetching Top Gainers as the default list for now
                val topGainers = repository.getTopGainers()
                
                val holdings = topGainers.map { quote ->
                    // Determine asset class (defaulting to STOCK for FMP gainers endpoint)
                    val assetClass = AssetClass.STOCK
                    
                    HoldingRowUi(
                        name = quote.name ?: quote.symbol,
                        assetClass = assetClass,
                        changePct = quote.changesPercentage,
                        price = quote.price,
                        dayLow = quote.dayLow ?: quote.price,
                        dayHigh = quote.dayHigh ?: quote.price,
                        logoUrl = LogoUtil.getLogoUrl(quote.symbol)
                    )
                }

                _state.value = _state.value.copy(
                    holdings = holdings,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Handle error
                _state.value = _state.value.copy(isLoading = false)
                println("Error loading home data: ${e.message}")
            }
        }
    }
}

data class HomeUiState(
    val holdings: List<HoldingRowUi> = emptyList(),
    val isLoading: Boolean = true
)
