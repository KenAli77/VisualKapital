package com.example.visualmoney.assetDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import com.example.visualmoney.navigation.Routes
import kotlinx.coroutines.launch

class AssetDetailsViewModel( private val repository: FinancialRepository) : ViewModel() {

    var asset by mutableStateOf(AssetProfile())
        private set
    var assetQuote by mutableStateOf(AssetQuote())
        private set

    var chartPoints by mutableStateOf(emptyList<ChartPoint>())
        private set

    fun loadSymbolData(symbol:String) {
        viewModelScope.launch {
            asset = repository.getProfile(symbol)
            assetQuote = repository.getQuote(symbol)
            chartPoints = repository.getChart(symbol)

        }
    }
}