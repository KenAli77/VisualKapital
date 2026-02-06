package com.example.visualmoney.home

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.visualmoney.AssetCategory
import com.example.visualmoney.BlueScaleVariants
import com.example.visualmoney.GreenScaleVariants
import com.example.visualmoney.GreyScaleVariants
import com.example.visualmoney.PrimaryVariants
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.isQuoteTracked
import com.example.visualmoney.data.repository.FinancialRepository
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.util.LogoUtil
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
                        val quoteMap: Map<String, AssetQuote> =
                            assets
                                .filter { it.isQuoteTracked }
                                .map { it.symbol }
                                .distinct()
                                .map { symbol ->
                                    async { symbol to repository.getQuote(symbol) }
                                }
                                .awaitAll()
                                .toMap()

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
    ): Pair<List<HoldingRowUi>, PortfolioMetrics> {

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

            HoldingRowUi(
                symbol = asset.symbol,
                name = asset.name,
                assetClass = asset.type,
                changePct = changePct,
                price = currentPrice,
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
    val holdings: List<HoldingRowUi> = emptyList(),
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

    val money = formatDecimal(
        value = abs(changeAbs),
        decimals = decimalsMoney,
        grouping = useGrouping
    )

    val pct = formatDecimal(
        value = abs(changePct),
        decimals = decimalsPct,
        grouping = false
    )

    // Choose how you want "flat" to look
    if (isFlat && flatAsZeroNoTriangle) {
        return "${currency}${formatDecimal(0.0, decimalsMoney, useGrouping)} (${
            formatDecimal(
                0.0,
                decimalsPct,
                false
            )
        }%)"
    }

//    val prefix = if (triangle.isNotEmpty()) "$triangle " else ""
    return "$sign$currency$money ($sign$pct%)"
}

private fun formatDecimal(
    value: Double,
    decimals: Int = 2,
    grouping: Boolean = true
): String {
    // Round to N decimals without relying on platform formatting
    val factor = pow10(decimals)
    val rounded = round(value * factor) / factor

    val whole = rounded.toLong()
    val frac = ((rounded - whole) * factor).let { round(it).toLong() }.coerceIn(0, factor - 1)

    val wholeStr = if (grouping) groupThousands(whole) else whole.toString()
    val fracStr = frac.toString().padStart(decimals, '0')

    return if (decimals == 0) wholeStr else "$wholeStr.$fracStr"
}

private fun pow10(n: Int): Long {
    var r = 1L
    repeat(n.coerceAtLeast(0)) { r *= 10L }
    return r
}

private fun groupThousands(value: Long): String {
    val s = value.toString()
    val out = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        out.append(s[i])
        count++
        if (count % 3 == 0 && i != 0) out.append(',')
    }
    return out.reverse().toString()
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

    val money = formatDecimal(
        value = abs(changeAbs),
    )

    val pct = formatDecimal(
        value = abs(changePct),
    )

    // Choose how you want "flat" to look
    if (isFlat) {
        return "${currency}${formatDecimal(0.0)} (${
            formatDecimal(
                0.0,
            )
        }%)"
    }

//    val prefix = if (triangle.isNotEmpty()) "$triangle " else ""
    return "$sign$currency$money ($sign$pct%)"

}
