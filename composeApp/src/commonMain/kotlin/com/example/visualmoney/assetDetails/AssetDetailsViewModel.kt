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
            val chartPeriod = state.selectedChartRange.apiPeriod
            val chart = repository.getChart(symbol, chartPeriod.start, chartPeriod.end)
            state = state.copy(
                profile = profile,
                quote = quote,
                chart = chart
            )

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
                    val period = event.period.apiPeriod
                    state.profile?.let { prof ->
                        val chart = repository.getChart(prof.symbol, period.start, period.end)

                        state = state.copy(
                            chart = chart
                        )

                    }
                }

                is AssetDetailEvent.RemoveFromPortfolio -> {

                }
            }
        }

    }
}