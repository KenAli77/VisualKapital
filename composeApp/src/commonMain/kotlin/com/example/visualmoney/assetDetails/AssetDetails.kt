package com.example.visualmoney.assetDetails


import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.visualmoney.DefaultAppColors
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.core.toApiDateString
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.logoUrl
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import com.example.visualmoney.domain.model.logoUrl
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.borderGradient
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.format
import com.example.visualmoney.home.theme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.arrow_back
import visualmoney.composeapp.generated.resources.plus
import visualmoney.composeapp.generated.resources.trash
import kotlin.math.max
import kotlin.time.Clock

private val theme @Composable get() = LocalAppTheme.current

// ---------- UI Models ----------
enum class ChartRange(val label: String) {
    //    ONE_DAY("1D"),
    ONE_WEEK("1W"), ONE_MONTH("1M"), THREE_MONTHS("3M"), ONE_YEAR("1Y")
}

data class ChartPeriod(
    val start: String, val end: String
)

val ChartRange.apiPeriod: ChartPeriod
    get() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return when (this) {
//            ChartRange.ONE_DAY -> {
//                ChartPeriod(today.minus(DatePeriod(days = 1)).toApiDateString(), today.toApiDateString())
//            }

            ChartRange.ONE_WEEK -> {
                val periodStart = today.minus(DatePeriod(days = 7))
                ChartPeriod(periodStart.toApiDateString(), today.toApiDateString())
            }

            ChartRange.ONE_MONTH -> {
                val periodStart = today.minus(DatePeriod(months = 1))
                ChartPeriod(periodStart.toApiDateString(), today.toApiDateString())

            }

            ChartRange.THREE_MONTHS -> {
                val periodStart = today.minus(DatePeriod(months = 3))
                ChartPeriod(periodStart.toApiDateString(), today.toApiDateString())
            }

            ChartRange.ONE_YEAR -> {
                val periodStart = today.minus(DatePeriod(years = 1))
                ChartPeriod(periodStart.toApiDateString(), today.toApiDateString())

            }
        }
    }


data class PortfolioPositionUi(
    val quantity: Double, val avgCost: Double, val currency: String = "USD"
)

// ---------- Screen ----------
@Composable
fun AssetDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: AssetDetailsViewModel,
    onBack: () -> Unit = {},
    onOpenWebsite: (String) -> Unit = {},
) = with(viewModel) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
                contentPadding = PaddingValues(
                    bottom = theme.dimension.veryLargeSpacing,

                    )
            ) {
                item {
                    TopNavigationBar(title = "Asset details", onBack = onBack)
                }

                item {
                    state.profile?.let { profile ->
                        state.asset?.let { asset ->
                            state.quote?.let { quote ->
                                PriceAndChartCard(
                                    asset = asset,
                                    quote = quote,
                                    profile = profile,
                                    chart = state.chart,
                                    selectedRange = state.selectedChartRange,
                                    onSelectRange = { onEvent(AssetDetailEvent.ChartPeriodChanged(it)) }
                                )
                            }
                        }
                    }
                }

                item {
                    PortfolioCard(
                        state = state
                    )
                }
                state.quote?.let {
                    item {
                        KeyStatsCard(quote = it, profile = state.profile ?: AssetProfile())
                    }

                }
                if (!state.profile?.description.isNullOrBlank()) {
                    state.profile?.let {
                        item {
                            AboutCard(
                                companyName = it.companyName ?: "",
                                industry = it.industry,
                                sector = it.sector,
                                country = it.country,
                                description = it.description,
                                website = it.website,
                                onOpenWebsite = onOpenWebsite
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Top bar ----------
@Composable
private fun AssetTopBar(
    title: String,
    subtitle: String,
    inPortfolio: Boolean,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            IconWithContainer(
                onClick = onBack,
                icon = painterResource(Res.drawable.arrow_back),
                containerColor = theme.colors.container
            )

            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)) {
                Text(
                    text = title,
                    style = theme.typography.bodyMediumMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c60
                )
            }
        }

        IconWithContainer(
            onClick = onPrimaryAction,
            icon = painterResource(if (inPortfolio) Res.drawable.trash else Res.drawable.plus),
            containerColor = if (inPortfolio) theme.colors.container else theme.colors.primary.c50
        )
    }
}

// ---------- Price + chart ----------
@Composable
private fun PriceAndChartCard(
    profile: AssetProfile,
    asset: PortfolioAsset,
    quote: AssetQuote,
    chart: List<ChartPoint>,
    selectedRange: ChartRange,
    onSelectRange: (ChartRange) -> Unit
) {
    println("Profile is: $profile")
    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = asset.symbol,
                            style = theme.typography.bodyLargeStrong,
                            color = theme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = asset.name,
                            style = theme.typography.bodySmallStrong,
                            color = theme.colors.greyTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(theme.dimension.verySmallRadius),
                        color = theme.colors.greyScale.c10
                    ) {
                        Text(
                            text = asset.exchangeName,
                            style = theme.typography.bodySmallMedium,
                            color = theme.colors.greyScale.c60,
                            modifier = Modifier.padding(
                                horizontal = theme.dimension.closeSpacing,
                                vertical = theme.dimension.veryCloseSpacing
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val price = "%.2f".format(quote.price)
                            Text(
                                text = price.substringBeforeLast("."),
                                style = theme.typography.titleLarge,
                                color = theme.colors.onSurface
                            )
                            Text(
                                text = "." + price.substringAfterLast("."),
                                style = theme.typography.titleLarge,
                                color = theme.colors.greyScale.c50
                            )
                        }
                        val pct = quote.changesPercentage
                        val absChange = quote.change
                        val pctChange = if (absChange >= 0) "+" else ""
                        val pctText = (if (pct >= 0) "+" else "") + "%.2f".format(pct) + "%"
                        Row {
                            Text(
                                text = "$pctText ($pctChange$absChange)",
                                style = theme.typography.bodySmallMedium,
                                color = if (pct >= 0) theme.colors.greenScale.c50 else theme.colors.error
                            )

                            Spacer(Modifier.width(theme.dimension.closeSpacing))
                            Text(
                                text = "Today",
                                style = theme.typography.bodySmall,
                                color = theme.colors.greyTextColor
                            )
                        }
                    }
                    AssetLogoContainer(asset.logoUrl, profile.symbol, size = 60.dp)
                }
            }
        }

        Sparkline(
            points = chart.map { it.price }, modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        ChartRangeTabs(selected = selectedRange, onSelect = onSelectRange)
    }

}

@Composable
private fun ChartRangeTabs(
    selected: ChartRange, onSelect: (ChartRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
    ) {
        ChartRange.entries.forEach { r ->
            val isSelected = r == selected
            val bg by animateColorAsState(
                if (isSelected) theme.colors.primary.c50 else theme.colors.surface,
                label = "tabBg"
            )
            val text = if (isSelected) theme.colors.onPrimary else theme.colors.greyTextColor

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(theme.dimension.defaultRadius),
                color = bg,
                border = borderStroke,
                onClick = { onSelect(r) }) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = theme.dimension.mediumSpacing)
                ) {
                    Text(r.label, style = theme.typography.bodySmallMedium, color = text)
                }
            }
        }
    }
}

@Composable
private fun Sparkline(
    points: List<Double>, modifier: Modifier = Modifier
) {
    val safe = remember(points) { points.filter { it.isFinite() } }
    val minV = remember(safe) { safe.minOrNull() ?: 0.0 }
    val maxV = remember(safe) { safe.maxOrNull() ?: 1.0 }
    val span = max(1e-9, maxV - minV)
    val gridLineCount = 4 // how many horizontal dashed lines

    val dashEffect = PathEffect.dashPathEffect(
        floatArrayOf(10f, 20f), // dash length, gap length
        0f
    )

    Column(modifier) {
        Canvas(
            modifier = Modifier.fillMaxHeight(0.9f).fillMaxWidth()
//                .background(theme.colors.surface)
        ) {
            if (safe.size < 2) return@Canvas
            repeat(gridLineCount) { index ->
                val y = size.height * (index + 1) / (gridLineCount + 1)

                drawLine(
                    color = DefaultAppColors.border.copy(0.4f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }
            val w = size.width
            val h = size.height
            val stepX = w / (safe.size - 1).toFloat()

            fun yFor(v: Double): Float {
                val t = ((v - minV) / span).toFloat()
                return (h - (t * h)).coerceIn(0f, h)
            }

            val path = Path()
            safe.forEachIndexed { i, v ->
                val x = i * stepX
                val y = yFor(v)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            val areaPath = Path().apply {
                addPath(path)                  // the sparkline path
                lineTo(w, h)                   // down to bottom-right
                lineTo(0f, h)                  // bottom-left
                close()
            }
            val fadeBrush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to DefaultAppColors.greenScale.c50.copy(alpha = 0.10f), // near line
                    0.5f to DefaultAppColors.greenScale.c50.copy(alpha = 0.05f),
                    1.0f to DefaultAppColors.greenScale.c50.copy(alpha = 0.0f)   // bottom
                ), startY = 0f, endY = h
            )

            drawPath(
                path = areaPath, brush = fadeBrush
            )

            // line
            drawPath(
                path = path, color = DefaultAppColors.greenScale.c50, style = Stroke(
                    width = 1.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round
                )
            )

        }
//        ListDivider()
//        Row(
//            modifier = Modifier.padding(vertical = theme.dimension.veryCloseSpacing)
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                "09AM",
//                style = theme.typography.bodySmallMedium,
//                color = theme.colors.greyTextColor
//            )
//            Text(
//                "10AM",
//                style = theme.typography.bodySmallMedium,
//                color = theme.colors.greyTextColor
//            )
//            Text(
//                "11AM",
//                style = theme.typography.bodySmallMedium,
//                color = theme.colors.greyTextColor
//            )
//            Text(
//                "12AM",
//                style = theme.typography.bodySmallMedium,
//                color = theme.colors.greyTextColor
//            )
//            Text(
//                "1PM",
//                style = theme.typography.bodySmallMedium,
//                color = theme.colors.greyTextColor
//            )
//        }
    }

}

// ---------- Portfolio card (view + add/remove + edit) ----------
private val primaryGradient = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFBC42), 0.5f to Color(0xFFFFC653), 1.0f to Color(0xFFFFA904)
    ), start = Offset(0f, 0f), end = Offset.Infinite
)

@Composable
private fun PortfolioCard(state: AssetDetailState) {
    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
        Text(
            "In my portfolio",
            style = theme.typography.bodyLargeMedium,
            color = theme.colors.greyTextColor
        )
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = theme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(theme.dimension.largeSpacing),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Current value",
                        style = theme.typography.bodyMediumMedium,
                        color = theme.colors.greyTextColor,
                    )
                    Text(
                        text = "${state.quote?.price}",
                        style = theme.typography.bodyMediumStrong,
                        color = theme.colors.onSurface,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Amount",
                        style = theme.typography.bodyMediumMedium,
                        color = theme.colors.greyTextColor,
                    )
                    Text(
                        text = "${state.asset?.qty}",
                        style = theme.typography.bodyMediumStrong,
                        color = theme.colors.onSurface,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total return",
                        style = theme.typography.bodyMediumMedium,
                        color = theme.colors.greyTextColor,
                    )
                    Text(
                        text = state.roiText,
                        style = theme.typography.bodyMediumStrong,
                        color = theme.colors.onSurface,
                    )
                }

            }
        }
    }
}


// ---------- Key stats ----------
@Composable
private fun KeyStatsCard(
    quote: AssetQuote, profile: AssetProfile?
) {

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
    ) {
        Text(
            "Key stats",
            style = theme.typography.bodyLargeMedium,
            color = theme.colors.greyTextColor
        )
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = theme.colors.surface
        ) {
            Box(modifier = Modifier.padding(theme.dimension.largeSpacing)) {
                StatGrid(
                    stats = listOfNotNull(
                        Stat("Day range", quote.dayLow?.let { low ->
                            quote.dayHigh?.let { high -> "${fmt2(low)} - ${fmt2(high)}" }
                        } ?: ""),
                        Stat("52w range", quote.yearLow?.let { low ->
                            quote.yearHigh?.let { high -> "${fmt2(low)} - ${fmt2(high)}" }
                        }),
                        Stat("Volume", quote.volume?.let { fmtCompact(it.toDouble()) }),
                        Stat("Market cap", quote.marketCap?.let { fmtCompact(it) }),
                    )
                )

            }
        }
    }
}

private data class Stat(val label: String, val value: String? = "")

@Composable
private fun StatGrid(stats: List<Stat>) {
    // 2-column grid using rows
    val rows = stats.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                row.forEach { s ->
                    StatCell(
                        label = s.label, value = s.value ?: "", modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String, value: String, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, style = theme.typography.bodySmall, color = theme.colors.greyTextColor
        )
        Text(
            value, style = theme.typography.bodySmallMedium, color = theme.colors.onSurface
        )
    }

}

// ---------- About ----------
@Composable
private fun AboutCard(
    companyName: String?,
    industry: String?,
    sector: String?,
    country: String?,
    description: String?,
    website: String?,
    onOpenWebsite: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
        Text(
            "About",
            style = theme.typography.bodyLargeMedium,
            color = theme.colors.greyTextColor
        )

        CardContainer(
            containerColor = theme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(theme.dimension.largeSpacing),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
            ) {
                Text(
                    companyName ?: "About",
                    style = theme.typography.titleSmallMedium,
                    color = theme.colors.onSurface
                )

                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = theme.typography.bodySmall,
                        color = theme.colors.greyTextColor,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "No description available.",
                        style = theme.typography.bodySmall,
                        color = theme.colors.greyTextColor
                    )
                }

                // Meta line
                val meta = listOfNotNull(
                    industry?.takeIf { it.isNotBlank() },
                    sector?.takeIf { it.isNotBlank() },
                    country?.takeIf { it.isNotBlank() }).joinToString(" • ")
                if (meta.isNotBlank()) {
                    Text(
                        meta,
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.onSurface
                    )
                }

                if (!website.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(theme.dimension.defaultRadius),
                        color = theme.colors.container,
                        onClick = { onOpenWebsite(website) }) {
                        Row(
                            modifier = Modifier.padding(theme.dimension.mediumSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.WorkspacePremium,
                                contentDescription = null,
                                modifier = Modifier.size(theme.dimension.smallIconSize),
                                tint = theme.colors.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = website,
                                style = theme.typography.bodySmallMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = theme.colors.onSurface
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(
                                Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(theme.dimension.smallIconSize),
                                tint = theme.colors.onSurface

                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Meta chips row ----------
@Composable
private fun MetaChipsRow(
    exchange: String?,
    ceo: String?,
    employees: String?,
    isin: String?,
) {
    val chips = listOfNotNull(
        exchange?.takeIf { it.isNotBlank() }?.let { "Exchange: $it" },
        ceo?.takeIf { it.isNotBlank() }?.let { "CEO: $it" },
        employees?.takeIf { it.isNotBlank() }?.let { "Employees: $it" },
        isin?.takeIf { it.isNotBlank() }?.let { "ISIN: $it" },
    )

    if (chips.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
    ) {
        chips.take(3).forEach { c ->
            Surface(
                shape = RoundedCornerShape(theme.dimension.verySmallRadius),
                color = theme.colors.greyScale.c10
            ) {
                Text(
                    text = c,
                    style = theme.typography.bodySmallMedium,
                    color = theme.colors.greyScale.c60,
                    modifier = Modifier.padding(
                        horizontal = theme.dimension.closeSpacing,
                        vertical = theme.dimension.veryCloseSpacing
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


// ---------- Formatting ----------
private fun fmt2(v: Double) = "%.2f".format(v)

private fun fmtCompact(v: Double): String {
    val abs = kotlin.math.abs(v)
    return when {
        abs >= 1_000_000_000 -> "%.2fB".format(v / 1_000_000_000)
        abs >= 1_000_000 -> "%.2fM".format(v / 1_000_000)
        abs >= 1_000 -> "%.2fK".format(v / 1_000)
        else -> "%.0f".format(v)
    }
}

fun ImageBitmap.averageColor(sampleSize: Int = 8): Color {
    val width = this.width
    val height = this.height

    var r = 0f
    var g = 0f
    var b = 0f
    var count = 0

    val stepX = maxOf(1, width / sampleSize)
    val stepY = maxOf(1, height / sampleSize)

    for (x in 0 until width step stepX) {
        for (y in 0 until height step stepY) {
            val pixel = this
//            r += pixel.red
//            g += pixel.green
//            b += pixel.blue
            count++
        }
    }

    return Color(r / count, g / count, b / count)
}

@Composable
fun AssetLogoContainer(
    logoUrl: String?,
    symbol: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier.size(size)
            .clip(RoundedCornerShape(theme.dimension.defaultRadius))
            .border(
                1.dp,
                brush = borderGradient,
                shape = RoundedCornerShape(theme.dimension.defaultRadius)
            )
            .background(borderGradient),
        contentAlignment = Alignment.Center
    ) {
        logoUrl?.let {
            AsyncImage(
                modifier = Modifier.padding(1.dp)
                    .clip(RoundedCornerShape(theme.dimension.defaultRadius)),
                model = logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        } ?: Text(
            text = symbol.take(1),
            style = theme.typography.bodyMediumMedium,
            fontWeight = FontWeight.Medium,

            )
    }
}