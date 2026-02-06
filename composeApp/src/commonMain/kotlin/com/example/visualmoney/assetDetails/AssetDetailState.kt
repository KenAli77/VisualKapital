package com.example.visualmoney.assetDetails

import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint

data class AssetDetailState(
    val asset: PortfolioAsset? = null,
    val selectedChartRange: ChartRange = ChartRange.ONE_DAY,
    val quote: AssetQuote? = AssetQuote(),
    val profile: AssetProfile? = AssetProfile(),
    val chart: List<ChartPoint> = emptyList(),
)
