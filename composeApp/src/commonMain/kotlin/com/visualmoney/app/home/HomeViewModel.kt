package com.visualmoney.app.home

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visualmoney.app.AssetCategory
import com.visualmoney.app.BlueScaleVariants
import com.visualmoney.app.GreenScaleVariants
import com.visualmoney.app.GreyScaleVariants
import com.visualmoney.app.PrimaryVariants
import com.visualmoney.app.data.local.PortfolioAsset
import com.visualmoney.app.data.local.isQuoteTracked
import com.visualmoney.app.data.repository.FinancialRepository
import com.visualmoney.app.domain.model.AssetQuote
import com.visualmoney.app.util.LogoUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.trending_down
import visualmoney.composeapp.generated.resources.trending_up
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

class HomeViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    var state by mutableStateOf(HomeUiState())
        private set

    init {
        observePortfolio()
    }

    private fun buildPortfolioDistribution(
        assets: List<PortfolioAsset>,
        quoteMap: Map<String, AssetQuote>,
        totalPortfolioValue: Double
    ): List<PortfolioDistributionItem> {

        if (totalPortfolioValue == 0.0) return emptyList()

        return assets
            .groupBy { it.type } // AssetCategory
            .map { (category, categoryAssets) ->

                val categoryValue = categoryAssets.sumOf { asset ->
                    val price = if (asset.isQuoteTracked) {
                        quoteMap[asset.symbol]?.price ?: asset.purchasePrice
                    } else {
                        asset.purchasePrice
                    }
                    asset.qty * price
                }
                val categoryPurchaseValue =
                    categoryAssets.sumOf { max(it.qty, 1) * it.purchasePrice }


                PortfolioDistributionItem(
                    category = category,
                    label = category.label,
                    totalValue = categoryValue,
                    purchaseValue = categoryPurchaseValue,
                    percentage = (categoryValue / totalPortfolioValue) * 100.0,
                    productCount = categoryAssets.size
                )
            }
            .sortedByDescending { it.totalValue }
    }

    private fun observePortfolio() {
        viewModelScope.launch {
            repository.getPortfolioAssets()
                .collect { assets ->
                    state = state.copy(isLoading = true)

                    try {
                        // 1) Fetch quotes for quote-tracked assets (in parallel)
                        // 1) Fetch quotes for quote-tracked assets (in parallel)
                        val quoteTrackedSymbols = assets
                                .filter { it.isQuoteTracked }
                                .map { it.symbol }
                                .distinct()
                        
                        val quotes = repository.getQuotes(quoteTrackedSymbols)
                        val quoteMap = quotes.associateBy { it.symbol }


                        // 2) Build UI rows + portfolio metrics
                        val (holdingRows, metrics) = buildHoldingsAndMetrics(assets, quoteMap)

                        val distribution = buildPortfolioDistribution(
                            assets = assets,
                            quoteMap = quoteMap,
                            totalPortfolioValue = metrics.totalValue
                        )

                        state = state.copy(
                            holdings = holdingRows,
                            metrics = metrics,
                            distribution = distribution,
                            isLoading = false
                        )
                    } catch (e: Exception) {
                        state = state.copy(isLoading = false)
                    }
                }
        }
    }

    private fun buildHoldingsAndMetrics(
        assets: List<PortfolioAsset>,
        quoteMap: Map<String, AssetQuote>
    ): Pair<List<AssetListItemUI>, PortfolioMetrics> {

        var totalValue = 0.0
        var totalCost = 0.0

        val rows = assets.map { asset ->
            val qty = asset.qty
            val cost = qty * asset.purchasePrice
            totalCost += cost

            val currentPrice = if (asset.isQuoteTracked) {
                quoteMap[asset.symbol]?.price ?: asset.purchasePrice
            } else {
                asset.purchasePrice // for non-quoted assets
            }

            val value = qty * currentPrice
            totalValue += value

            val changePct = if (asset.purchasePrice != 0.0) {
                ((currentPrice - asset.purchasePrice) / asset.purchasePrice) * 100.0
            } else 0.0

            AssetListItemUI(
                symbol = asset.symbol,
                name = asset.name,
                assetClass = asset.type,
                changePct = changePct,
                price = currentPrice,
                note = asset.notes ?: asset.note,
                dayLow = quoteMap[asset.symbol]?.dayLow ?: currentPrice,
                dayHigh = quoteMap[asset.symbol]?.dayHigh ?: currentPrice,
                logoUrl = LogoUtil.getLogoUrl(asset.symbol)
            )
        }

        val changeAbs = totalValue - totalCost
        val changePct = if (totalCost != 0.0) (changeAbs / totalCost) * 100.0 else 0.0

        return rows to PortfolioMetrics(
            totalValue = totalValue,
            totalCost = totalCost,
            changeAbs = changeAbs,
            changePct = changePct
        )
    }
}

data class HomeUiState(
    val holdings: List<AssetListItemUI> = emptyList(),
    val isLoading: Boolean = true,
    val distribution: List<PortfolioDistributionItem> = emptyList(),
    val metrics: PortfolioMetrics = PortfolioMetrics(),
)

data class PortfolioMetrics(
    val totalValue: Double = 0.0, // current market value
    val totalCost: Double = 0.0,       // cost basis
    val changeAbs: Double = 0.0,       // totalValue - totalCost
    val changePct: Double = 0.0        // changeAbs / totalCost * 100
)

val PortfolioMetrics.isPositive get() = changeAbs > 0.0
val PortfolioMetrics.isNegative get() = changeAbs < 0.0
val PortfolioMetrics.isFlat get() = changeAbs == 0.0

val PortfolioMetrics.color
    @Composable get() = when {
        isPositive -> theme.colors.greenScale.c50
        isNegative -> theme.colors.error
        else -> theme.colors.onSurface
    }

@Composable
fun PortfolioMetrics.ChangeIcon() = when {
    isNegative -> Icon(
        painterResource(Res.drawable.trending_down),
        tint = theme.colors.error,
        contentDescription = null,
        modifier = Modifier.size(theme.dimension.smallIconSize)
    )

    else -> Icon(
        painterResource(Res.drawable.trending_up),
        tint = theme.colors.greenScale.c50,
        contentDescription = null,
        modifier = Modifier.size(theme.dimension.smallIconSize)
    )
}

fun PortfolioMetrics.toChangeUiString(
    currency: String,                 // e.g. "$", "€", "USD "
    decimalsMoney: Int = 2,
    decimalsPct: Int = 2,
    useGrouping: Boolean = true,
    showPlusSign: Boolean = true,
    flatAsZeroNoTriangle: Boolean = true
): String {
    val isPositive = changeAbs > 0.0
    val isNegative = changeAbs < 0.0
    val isFlat = !isPositive && !isNegative

    val sign = when {
        isPositive && showPlusSign -> "+"
        isNegative -> "-"
        else -> ""
    }

    val money = com.visualmoney.app.util.formatDecimal(
        value = abs(changeAbs),
        decimals = decimalsMoney,
        grouping = useGrouping
    )

    val pct = com.visualmoney.app.util.formatDecimal(
        value = abs(changePct),
        decimals = decimalsPct,
        grouping = false
    )

    // Choose how you want "flat" to look
    if (isFlat && flatAsZeroNoTriangle) {
        return "${currency}${com.visualmoney.app.util.formatDecimal(0.0, decimalsMoney, useGrouping)} (${
            com.visualmoney.app.util.formatDecimal(
                0.0,
                decimalsPct,
                false
            )
        }%)"
    }

//    val prefix = if (triangle.isNotEmpty()) "$triangle " else ""
    return "$sign$currency$money ($sign$pct%)"
}


data class PortfolioDistributionItem(
    val category: AssetCategory,
    val label: String,
    val purchaseValue: Double,
    val totalValue: Double,
    val percentage: Double,
    val productCount: Int,
)

val PortfolioDistributionItem.color: Color
    get() =
        when (category) {
            AssetCategory.STOCKS -> GreenScaleVariants.c50
            AssetCategory.CRYPTO -> PrimaryVariants.c50
            AssetCategory.COMMODITIES -> BlueScaleVariants.c50
            AssetCategory.OTHER -> GreyScaleVariants.c50
        }

val PortfolioDistributionItem.trackColor: Color
    get() = when (category) {
        AssetCategory.STOCKS -> GreenScaleVariants.c40.copy(alpha = 0.4f)
        AssetCategory.CRYPTO -> PrimaryVariants.c40.copy(alpha = 0.4f)
        AssetCategory.COMMODITIES -> BlueScaleVariants.c40.copy(alpha = 0.4f)
        AssetCategory.OTHER -> GreyScaleVariants.c40.copy(alpha = 0.4f)
    }


fun PortfolioDistributionItem.categoryPerformanceString(currency: String): String {
    val changeAbs = totalValue - purchaseValue
    val changePct = if (purchaseValue != 0.0) ((changeAbs / purchaseValue) * 100.0) else 0.0
    val isPositive = changeAbs > 0.0
    val isNegative = changeAbs < 0.0
    val isFlat = !isPositive && !isNegative

    val sign = when {
        isPositive -> "+"
        isNegative -> "-"
        else -> ""
    }

    val money = com.visualmoney.app.util.formatDecimal(
        value = abs(changeAbs),
    )

    val pct = com.visualmoney.app.util.formatDecimal(
        value = abs(changePct),
    )

    // Choose how you want "flat" to look
    if (isFlat) {
        return "${currency}${com.visualmoney.app.util.formatDecimal(0.0)} (${
            com.visualmoney.app.util.formatDecimal(
                0.0,
            )
        }%)"
    }

//    val prefix = if (triangle.isNotEmpty()) "$triangle " else ""
    return "$sign$currency$money ($sign$pct%)"

}
