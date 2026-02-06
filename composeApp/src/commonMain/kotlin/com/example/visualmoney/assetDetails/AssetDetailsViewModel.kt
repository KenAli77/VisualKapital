package com.example.visualmoney.assetDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.repository.FinancialRepository
import kotlinx.coroutines.launch

class AssetDetailsViewModel(private val repository: FinancialRepository) : ViewModel() {

    var state by mutableStateOf(AssetDetailState())
        private set


    fun loadSymbolData(symbol: String) {
        viewModelScope.launch {
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
            state = state.copy(
                profile = profile,
                quote = quote,
                selectedChartRange = ChartRange.ONE_YEAR,
                chart1W = chart1W,
                chart1Y = chart1Y,
                chart1M = chart1M,
                chart3M = chart3M,
            )

        }
        viewModelScope.launch {
            repository.getPortfolioAsset(symbol).collect {
                state = state.copy(
                    asset = it
                )
            }
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