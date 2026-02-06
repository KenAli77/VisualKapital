package com.example.visualmoney.assetDetails

import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import com.example.visualmoney.home.format

data class AssetDetailState(
    val asset: PortfolioAsset? = null,
    val selectedChartRange: ChartRange = ChartRange.ONE_YEAR,
    val quote: AssetQuote? = AssetQuote(),
    val profile: AssetProfile? = AssetProfile(),
    val chart1W: List<ChartPoint> = emptyList(),
    val chart1M: List<ChartPoint> = emptyList(),
    val chart3M: List<ChartPoint> = emptyList(),
    val chart1Y: List<ChartPoint> = emptyList(),
)
val AssetDetailState.roiText:String get() {
    val roi = (quote?.price ?: 0.0) / (asset?.purchasePrice ?: 1.0) - 1
    return "%.2f%%".format(roi * 100) + "%"
}
val AssetDetailState.chart: List<ChartPoint> get() {
    return when(selectedChartRange){
        ChartRange.ONE_WEEK -> chart1W.reversed()
        ChartRange.ONE_MONTH -> chart1M.reversed()
        ChartRange.THREE_MONTHS -> chart3M.reversed()
        ChartRange.ONE_YEAR -> chart1Y.reversed()
    }
}
