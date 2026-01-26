package com.example.visualmoney.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.local.AssetType
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.data.repository.PortfolioHoldingWithQuote
import com.example.visualmoney.util.LogoUtil
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    init {
        loadPortfolioData()
    }

    private fun loadPortfolioData() {
        println("HomeViewModel: Starting to load portfolio data")
        viewModelScope.launch {
            repository.observePortfolioWithQuotes()
                .catch { e ->
                    println("HomeViewModel: Error loading portfolio: ${e.message}")
                    e.printStackTrace()
                    state = state.copy(isLoading = false, error = e.message)
                }
                .collect { holdings ->
                    println("HomeViewModel: Received ${holdings.size} holdings from repository")
                    
                    val uiHoldings = holdings.map { it.toHoldingRowUi() }
                    
                    // Calculate portfolio summary
                    val totalValue = holdings.sumOf { it.currentPrice * it.totalQuantity }
                    val totalCostBasis = holdings.sumOf { it.costBasis }
                    val profitLoss = totalValue - totalCostBasis
                    val profitLossPct = if (totalCostBasis > 0) (profitLoss / totalCostBasis) * 100 else 0.0
                    
                    println("HomeViewModel: Portfolio - Value=${"%.2f".format(totalValue)}, CostBasis=${"%.2f".format(totalCostBasis)}, P/L=${"%.2f".format(profitLoss)} (${"+".takeIf { profitLossPct >= 0 } ?: ""}${"%.2f".format(profitLossPct)}%)")
                    
                    val topMovers = uiHoldings.sortedByDescending { abs(it.changePct) }
                    val gainers = uiHoldings.filter { it.changePct > 0 }.sortedByDescending { it.changePct }
                    val losers = uiHoldings.filter { it.changePct < 0 }.sortedBy { it.changePct }
                    
                    state = state.copy(
                        totalValue = totalValue,
                        totalCostBasis = totalCostBasis,
                        profitLoss = profitLoss,
                        profitLossPct = profitLossPct,
                        topMovers = topMovers,
                        gainers = gainers,
                        losers = losers,
                        h24Data = uiHoldings,
                        isLoading = false,
                        error = null
                    )
                    println("HomeViewModel: State updated with portfolio summary")
                }
        }
    }

    private fun PortfolioHoldingWithQuote.toHoldingRowUi(): HoldingRowUi {
        return HoldingRowUi(
            symbol = symbol,
            name = name ?: symbol,
            assetClass = type.toAssetClass(),
            changePct = changePct,
            price = currentPrice,
            dayLow = dayLow ?: currentPrice,
            dayHigh = dayHigh ?: currentPrice,
            logoUrl = LogoUtil.getLogoUrl(symbol)
        )
    }

    private fun AssetType.toAssetClass(): AssetClass {
        return when (this) {
            AssetType.EQUITY -> AssetClass.STOCK
            AssetType.CRYPTO -> AssetClass.CRYPTO
            AssetType.COMMODITY -> AssetClass.STOCK // Map commodity to stock for now
        }
    }
}

data class HomeUiState(
    val totalValue: Double = 0.0,
    val totalCostBasis: Double = 0.0,
    val profitLoss: Double = 0.0,
    val profitLossPct: Double = 0.0,
    val topMovers: List<HoldingRowUi> = emptyList(),
    val gainers: List<HoldingRowUi> = emptyList(),
    val losers: List<HoldingRowUi> = emptyList(),
    val h24Data: List<HoldingRowUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
