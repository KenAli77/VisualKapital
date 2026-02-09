package com.example.visualmoney.assetDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.LoadingManager
import com.example.visualmoney.data.repository.FinancialRepository
import com.revenuecat.purchases.kmp.Purchases
import kotlinx.coroutines.launch

class AssetDetailsViewModel(
    private val repository: FinancialRepository,
    private val purchases: Purchases
) : ViewModel() {

    var state by mutableStateOf(AssetDetailState())
        private set


    override fun onCleared() {
        LoadingManager.stopLoading()
        super.onCleared()
    }
    
    private fun checkPremiumStatus() {
        purchases.getCustomerInfo(
            onSuccess = { customerInfo ->
                val isPremium = customerInfo.entitlements.active.isNotEmpty()
                state = state.copy(isPremium = isPremium)
            },
            onError = { _ -> }
        )
    }
    
    fun loadSymbolData(symbol: String) {
        viewModelScope.launch {
            LoadingManager.startLoading()
            
            // Check premium status
            checkPremiumStatus()
            
            viewModelScope.launch {
                repository.getPortfolioAsset(symbol).collect {
                    state = state.copy(
                        asset = it
                    )
                }
            }
            val profile = repository.getProfile(symbol)
            val quote = repository.getQuote(symbol)
            val chart1W = repository.getChart(
                symbol,
                ChartRange.ONE_WEEK.apiPeriod.start,
                ChartRange.ONE_WEEK.apiPeriod.end
            )
            val chart1M = repository.getChart(
                symbol,
                ChartRange.ONE_MONTH.apiPeriod.start,
                ChartRange.ONE_MONTH.apiPeriod.end
            )
            val chart3M = repository.getChart(
                symbol,
                ChartRange.THREE_MONTHS.apiPeriod.start,
                ChartRange.THREE_MONTHS.apiPeriod.end
            )
            val chart1Y = repository.getChart(
                symbol,
                ChartRange.ONE_YEAR.apiPeriod.start,
                ChartRange.ONE_YEAR.apiPeriod.end
            )
            
            // Load news for this asset
            state = state.copy(isNewsLoading = true)
            val news = try {
                repository.getStockNews(listOf(symbol))
            } catch (e: Exception) {
                emptyList()
            }
            
            state = state.copy(
                profile = profile,
                quote = quote,
                selectedChartRange = ChartRange.ONE_YEAR,
                chart1W = chart1W,
                chart1Y = chart1Y,
                chart1M = chart1M,
                chart3M = chart3M,
                news = news,
                isNewsLoading = false
            )
            LoadingManager.stopLoading()
        }

    }

    fun onEvent(event: AssetDetailEvent) {
        viewModelScope.launch {
            when (event) {
                is AssetDetailEvent.AddToPortfolio -> {

                }

                is AssetDetailEvent.ChartPeriodChanged -> {
                    state = state.copy(
                        selectedChartRange = event.period
                    )

                }

                is AssetDetailEvent.RemoveFromPortfolio -> {

                }
            }
        }

    }
}