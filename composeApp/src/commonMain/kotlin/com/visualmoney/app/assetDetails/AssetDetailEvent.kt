package com.visualmoney.app.assetDetails

sealed interface AssetDetailEvent {
    object AddToPortfolio : AssetDetailEvent
    object RemoveFromPortfolio : AssetDetailEvent
    data class ChartPeriodChanged(val period: ChartRange): AssetDetailEvent
}