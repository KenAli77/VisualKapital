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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.visualmoney.AssetCategory
import com.example.visualmoney.DefaultAppColors
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.calendar.EmptyStateCard
import com.example.visualmoney.calendar.format
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.core.toApiDateString
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.isQuoteTracked
import com.example.visualmoney.data.local.logoUrl
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import com.example.visualmoney.domain.model.logoUrl
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.AssetDetailTabs
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.borderGradient
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.format
import com.example.visualmoney.home.theme
import com.example.visualmoney.newAsset.event.AssetInputEvent
import com.example.visualmoney.shimmer
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
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import com.example.visualmoney.domain.model.StockNews

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
    var selectedTab by remember { mutableStateOf(AssetDetailTabs.About) }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
                contentPadding = PaddingValues(bottom = theme.dimension.veryLargeSpacing)
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
                                    onSelectRange = {
                                        onEvent(
                                            AssetDetailEvent.ChartPeriodChanged(
                                                it
                                            )
                                        )
                                    }
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
                item {
                    Column(modifier = Modifier.heightIn(400.dp)) {
                        SecondaryTabRow(
                            selectedTabIndex = selectedTab.ordinal,
                            modifier = Modifier.fillMaxWidth(),
                            contentColor = theme.colors.onSurface,
                            containerColor = Color.Transparent,
                            divider = { ListDivider() },
                            indicator = {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(
                                        selectedTab.ordinal,
                                        matchContentSize = false
                                    )
                                        .clip(RoundedCornerShape(theme.dimension.verySmallRadius)),
                                    color = theme.colors.primary.c50,
                                )
                            }
                        ) {
                            AssetDetailTabs.entries.forEach { currentTab ->
                                Tab(
                                    modifier = Modifier.clip(
                                        RoundedCornerShape(
                                            topStart = theme.dimension.defaultRadius,
                                            topEnd = theme.dimension.defaultRadius
                                        )
                                    ), selected = currentTab == currentTab, onClick = {
                                        selectedTab = currentTab
                                    }) {
                                    Row(
                                        modifier = Modifier.padding(vertical = theme.dimension.veryLargeSpacing),
                                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentTab.label,
                                            style = theme.typography.bodyMediumStrong
                                        )
                                    }
                                }
                            }
                        }

                        when (selectedTab) {
                            AssetDetailTabs.Stats -> {
                                if (state.asset?.isQuoteTracked == true) {
                                    state.quote?.let {
                                        KeyStatsCard(
                                            quote = it,
                                            profile = state.profile ?: AssetProfile()
                                        )
                                    } ?: EmptyStateCard(title = "No stats available")

                                } else {
                                    Column(
                                        modifier = Modifier,
                                        verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
                                    ) {
                                        Text("Added on ${state.asset?.purchasedAt?.format()}")
                                    }
                                }
                            }

                            AssetDetailTabs.About -> {
                                state.profile?.let {
                                    AboutCard(
                                        profile = it,
                                        onOpenWebsite = onOpenWebsite
                                    )
                                } ?: EmptyStateCard(title = "No information available")
                            }
                            
                            AssetDetailTabs.News -> {
                                if (state.isPremium) {
                                    NewsTabContent(
                                        news = state.news,
                                        isLoading = state.isNewsLoading
                                    )
                                } else {
                                    PremiumLockedContent()
                                }
                            }
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
                    if (asset.exchangeName.isNotBlank()) {
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
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val quotePrice =
                                if (asset.isQuoteTracked) quote.price else asset.currentPrice
                            val price = "%.2f".format(quotePrice)
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
                        if (asset.isQuoteTracked) {
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
                    }
                    AssetLogoContainer(asset.logoUrl, asset.symbol, size = 60.dp)
                }
            }
        }
        if (asset.isQuoteTracked) {
            Sparkline(
                points = chart.map { it.price }, modifier = Modifier.fillMaxWidth().height(200.dp)
            )

            ChartRangeTabs(selected = selectedRange, onSelect = onSelectRange)
        }
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

    }
}


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
                val currentValue = if (state.asset?.isQuoteTracked == true) {
                    state.quote?.price ?: state.asset.currentPrice
                } else {
                    state.asset?.currentPrice
                }
                val purchaseValue = state.asset?.purchasePrice
                if (purchaseValue != null) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Purchase price",
                            style = theme.typography.bodyMediumMedium,
                            color = theme.colors.greyTextColor,
                        )
                        Text(
                            text = "$purchaseValue",
                            style = theme.typography.bodyMediumStrong,
                            color = theme.colors.onSurface,
                        )
                    }
                }
                if (currentValue != null) {
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
                            text = "$currentValue",
                            style = theme.typography.bodyMediumStrong,
                            color = theme.colors.onSurface,
                        )
                    }
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
    profile: AssetProfile,
    onOpenWebsite: (String) -> Unit
) = with(profile) {
    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            companyName?.let {
                Text(
                    companyName,
                    style = theme.typography.titleSmallMedium,
                    color = theme.colors.onSurface
                )
            }
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

// ---------- News Tab ----------
@Composable
private fun NewsTabContent(
    news: List<StockNews>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = theme.colors.primary.c50)
        }
        return
    }
    
    if (news.isEmpty()) {
        EmptyStateCard(title = "No news available for this asset")
        return
    }
    
    Column(
        modifier = Modifier.padding(vertical = theme.dimension.mediumSpacing),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        news.forEach { newsItem ->
            NewsCard(newsItem)
        }
    }
}

@Composable
private fun NewsCard(news: StockNews) {
    CardContainer(
        modifier = Modifier.fillMaxWidth(),
        containerColor = theme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // News Image
            AsyncImage(
                model = news.image,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
            ) {
                Text(
                    text = news.title,
                    style = theme.typography.bodyMediumStrong,
                    color = theme.colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${news.site} • ${news.publishedDate.take(10)}",
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
            }
        }
    }
}

// ---------- Premium Locked Content ----------
@Composable
private fun PremiumLockedContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // Blurred background placeholder
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp)
                .padding(theme.dimension.mediumSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(theme.dimension.defaultRadius))
                        .background(theme.colors.surface)
                )
            }
        }
        
        // Lock overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.colors.greyScale.c80),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier.size(28.dp),
                    tint = theme.colors.onSurface
                )
            }
            Text(
                "Premium Feature",
                style = theme.typography.bodyMediumStrong,
                color = theme.colors.onSurface
            )
            Text(
                "Unlock premium to see news for this asset",
                style = theme.typography.bodySmall,
                color = theme.colors.greyScale.c50
            )
        }
    }
}