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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.visualmoney.DefaultAppColors
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.domain.model.Asset
import com.example.visualmoney.domain.model.AssetProfile
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.ChartPoint
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.IconWithContainerSmall
import com.example.visualmoney.home.format
import kotlin.math.max

private val theme @Composable get() = LocalAppTheme.current

// ---------- UI Models ----------
enum class ChartRange(val label: String) { D1("Days"), W1("Weeks"), M1("Months"), Y1("Years"), ALL("All") }

data class PortfolioPositionUi(
    val quantity: Double,
    val avgCost: Double,
    val currency: String = "USD"
)

// ---------- Screen ----------
@Composable
fun AssetDetailsScreen(
    modifier: Modifier = Modifier,
    asset: Asset,
    quote: AssetQuote,
    profile: AssetProfile? = null,
    chart: List<ChartPoint> = emptyList(),
    inPortfolio: Boolean = false,
    position: PortfolioPositionUi? = null,
    onBack: () -> Unit = {},
    onAddToPortfolio: () -> Unit = {},
    onRemoveFromPortfolio: () -> Unit = {},
    onEditPosition: (quantity: Double, avgCost: Double) -> Unit = { _, _ -> },
    onOpenWebsite: (String) -> Unit = {},
) {
    var selectedRange by remember { mutableStateOf(ChartRange.M1) }
    var qtyText by remember(position) { mutableStateOf(position?.quantity?.toString() ?: "") }
    var avgText by remember(position) { mutableStateOf(position?.avgCost?.toString() ?: "") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = theme.colors.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
            contentPadding = PaddingValues(
                top = theme.dimension.pagePadding,
                bottom = theme.dimension.pagePadding
            )
        ) {
            item {
                AssetTopBar(
                    title = asset.name ?: asset.symbol,
                    subtitle = asset.symbol,
                    inPortfolio = inPortfolio,
                    onBack = onBack,
                    onPrimaryAction = {
                        if (inPortfolio) onRemoveFromPortfolio() else onAddToPortfolio()
                    }
                )
            }

            item {
                PriceAndChartCard(
                    asset = asset,
                    quote = quote,
                    currency = profile?.currency ?: quote.exchange ?: "USD",
                    chart = chart,
                    selectedRange = selectedRange,
                    onSelectRange = { selectedRange = it }
                )
            }

            item {
                PortfolioCard(
                    inPortfolio = inPortfolio,
                    currency = profile?.currency ?: "USD",
                    qtyText = qtyText,
                    avgText = avgText,
                    onQtyChange = { qtyText = it },
                    onAvgChange = { avgText = it },
                    onAdd = onAddToPortfolio,
                    onRemove = onRemoveFromPortfolio,
                    onSave = {
                        val q = qtyText.toDoubleOrNull() ?: 0.0
                        val a = avgText.toDoubleOrNull() ?: 0.0
                        onEditPosition(q, a)
                    }
                )
            }

            item {
                KeyStatsCard(quote = quote, profile = profile)
            }

            item {
                AboutCard(
                    companyName = profile?.companyName ?: quote.name ?: asset.name,
                    industry = profile?.industry,
                    sector = profile?.sector,
                    country = profile?.country,
                    description = profile?.description,
                    website = profile?.website,
                    onOpenWebsite = onOpenWebsite
                )
            }

            // Optional: a few “facts” chips (keeps the look rich even if description is null)
            if (profile != null) {
                item {
                    MetaChipsRow(
                        exchange = profile.exchangeShortName ?: profile.exchange,
                        ceo = profile.ceo,
                        employees = profile.fullTimeEmployees,
                        isin = profile.isin
                    )
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
                icon = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
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
            icon = if (inPortfolio) Icons.Rounded.DeleteOutline else Icons.Rounded.Add,
            contentDescription = if (inPortfolio) "Remove from portfolio" else "Add to portfolio",
            containerColor = if (inPortfolio) theme.colors.container else theme.colors.primary.c50
        )
    }
}

// ---------- Price + chart ----------
@Composable
private fun PriceAndChartCard(
    asset: Asset,
    quote: AssetQuote,
    currency: String,
    chart: List<ChartPoint>,
    selectedRange: ChartRange,
    onSelectRange: (ChartRange) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.container)
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                    ) {
                        Text(
                            text = asset.name ?: asset.symbol,
                            style = theme.typography.bodySmallMedium,
                            color = theme.colors.greyTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(theme.dimension.verySmallRadius),
                            color = theme.colors.greyScale.c10
                        ) {
                            Text(
                                text = "Asset",
                                style = theme.typography.bodySmallMedium,
                                color = theme.colors.greyScale.c60,
                                modifier = Modifier.padding(
                                    horizontal = theme.dimension.closeSpacing,
                                    vertical = theme.dimension.veryCloseSpacing
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val price = "%.2f".format(quote.price)
                        Text(
                            text = price.substringBeforeLast("."),
                            style = theme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = theme.colors.onSurface
                        )
                        Text(
                            text = "." + price.substringAfterLast("."),
                            style = theme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = theme.colors.greyScale.c50
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = currency,
                            style = theme.typography.bodySmall,
                            color = theme.colors.greyTextColor
                        )
                    }

                    val pct = quote.changePercentage
                    val pctText = (if (pct >= 0) "+" else "") + "%.2f".format(pct) + "%"
                    Text(
                        text = pctText,
                        style = theme.typography.bodySmallMedium,
                        color = if (pct >= 0) theme.colors.greenScale.c50 else theme.colors.error
                    )
                }

                IconWithContainerSmall(
                    onClick = {},
                    icon = Icons.Rounded.NorthEast,
                    contentDescription = "Open",
                    containerColor = theme.colors.primary.c50,
                    contentColor = theme.colors.onSurface,
                    shape = CircleShape
                )
            }

            Sparkline(
                points = chart.map { it.price },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            ChartRangeTabs(selected = selectedRange, onSelect = onSelectRange)
        }
    }
}

@Composable
private fun ChartRangeTabs(
    selected: ChartRange,
    onSelect: (ChartRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
    ) {
        ChartRange.entries.forEach { r ->
            val isSelected = r == selected
            val bg by animateColorAsState(
                if (isSelected) theme.colors.onPrimary else theme.colors.surface,
                label = "tabBg"
            )
            val border = if (isSelected) Color.Transparent else theme.colors.greyScale.c30
            val text = if (isSelected) theme.colors.onSurface else theme.colors.greyTextColor

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(theme.dimension.smallRadius),
                color = bg,
                border = BorderStroke(1.dp, border),
                onClick = { onSelect(r) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Text(r.label, style = theme.typography.bodySmallMedium, color = text)
                }
            }
        }
    }
}

@Composable
private fun Sparkline(
    points: List<Double>,
    modifier: Modifier = Modifier
) {
    val safe = remember(points) { points.filter { it.isFinite() } }
    val minV = remember(safe) { safe.minOrNull() ?: 0.0 }
    val maxV = remember(safe) { safe.maxOrNull() ?: 1.0 }
    val span = max(1e-9, maxV - minV)

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(theme.dimension.defaultRadius))
            .background(theme.colors.surface)
            .border(1.dp, theme.colors.greyScale.c30, RoundedCornerShape(theme.dimension.defaultRadius))
            .padding(12.dp)
    ) {
        if (safe.size < 2) return@Canvas

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

        // faint baseline
        drawLine(
            color = DefaultAppColors.greyScale.c30,
            start = Offset(0f, h),
            end = Offset(w, h),
            strokeWidth = 1.dp.toPx()
        )

        // line
        drawPath(
            path = path,
            color = DefaultAppColors.greenScale.c50,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
        )
    }
}

// ---------- Portfolio card (view + add/remove + edit) ----------
private val primaryGradient = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFBC42),
        0.5f to Color(0xFFFFC653),
        1.0f to Color(0xFFFFA904)
    ),
    start = Offset(0f, 0f),
    end = Offset.Infinite
)

@Composable
private fun PortfolioCard(
    inPortfolio: Boolean,
    currency: String,
    qtyText: String,
    avgText: String,
    onQtyChange: (String) -> Unit,
    onAvgChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onSave: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        border = BorderStroke(1.dp, theme.colors.greyScale.c30),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Portfolio", style = theme.typography.titleSmallMedium)
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(theme.dimension.verySmallRadius),
                    color = theme.colors.greyScale.c10
                ) {
                    Text(
                        text = if (inPortfolio) "In portfolio" else "Not added",
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.greyScale.c60,
                        modifier = Modifier.padding(
                            horizontal = theme.dimension.closeSpacing,
                            vertical = theme.dimension.veryCloseSpacing
                        )
                    )
                }
            }

            // Inputs (edit allowed, but no trading)
            Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                CompactTextField(
                    label = "Quantity",
                    value = qtyText,
                    onValueChange = onQtyChange,
                    modifier = Modifier.weight(1f)
                )
                CompactTextField(
                    label = "Avg cost ($currency)",
                    value = avgText,
                    onValueChange = onAvgChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                if (!inPortfolio) {
                    GradientButton(
                        title = "Add to portfolio",
                        icon = Icons.Rounded.Add,
                        onClick = onAdd,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(theme.dimension.defaultRadius),
                        color = theme.colors.container,
                        onClick = onSave
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(theme.dimension.smallIconSize))
                            Spacer(Modifier.width(8.dp))
                            Text("Save changes", style = theme.typography.bodySmallMedium)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(theme.dimension.defaultRadius),
                        color = theme.colors.surface,
                        border = BorderStroke(1.dp, theme.colors.greyScale.c30),
                        onClick = onRemove
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(theme.dimension.smallIconSize))
                            Spacer(Modifier.width(8.dp))
                            Text("Remove", style = theme.typography.bodySmallMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(RoundedCornerShape(theme.dimension.defaultRadius))
            .border(1.dp, theme.colors.greyScale.c30, RoundedCornerShape(theme.dimension.defaultRadius)),
        singleLine = true,
        placeholder = { Text(label, style = theme.typography.bodySmall, color = theme.colors.greyTextColor) },
        textStyle = theme.typography.bodySmallMedium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = theme.colors.surface,
            unfocusedContainerColor = theme.colors.surface,
            disabledContainerColor = theme.colors.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun GradientButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(theme.dimension.defaultRadius))
            .background(primaryGradient),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(theme.dimension.smallIconSize))
            Spacer(Modifier.width(8.dp))
            Text(title, style = theme.typography.bodySmallMedium)
        }
    }
}

// ---------- Key stats ----------
@Composable
private fun KeyStatsCard(
    quote: AssetQuote,
    profile: AssetProfile?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        border = BorderStroke(1.dp, theme.colors.greyScale.c30),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            Text("Key stats", style = theme.typography.titleSmallMedium)

            StatGrid(
                stats = listOfNotNull(
                    Stat("Day range", quote.dayLow?.let { low ->
                        quote.dayHigh?.let { high -> "${fmt2(low)} - ${fmt2(high)}" }
                    } ?: ""),
                    Stat("52w range", quote.yearLow?.let { low ->
                        quote.yearHigh?.let { high -> "${fmt2(low)} - ${fmt2(high)}" }
                    }),
                    Stat("Volume", quote.volume?.let { fmtCompact(it.toDouble()) }),
                    Stat("Avg vol", quote.avgVolume?.let { fmtCompact(it.toDouble()) }),
                    Stat("Market cap", quote.marketCap?.let { fmtCompact(it.toDouble()) }),
                    Stat("P/E", quote.pe?.let { fmt2(it) }),
                    Stat("EPS", quote.eps?.let { fmt2(it) }),
                    Stat("Beta", profile?.beta?.let { fmt2(it) }),
                    Stat("Last div", profile?.lastDiv?.let { fmt2(it) })
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
                        label = s.label,
                        value = s.value ?: "",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        color = theme.colors.container
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
        ) {
            Text(label, style = theme.typography.bodySmall, color = theme.colors.greyTextColor)
            Text(value, style = theme.typography.bodySmallMedium, color = theme.colors.onSurface)
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        border = BorderStroke(1.dp, theme.colors.greyScale.c30),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            Text(companyName ?: "About", style = theme.typography.titleSmallMedium)

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
                country?.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(meta, style = theme.typography.bodySmallMedium, color = theme.colors.onSurface)
            }

            if (!website.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(theme.dimension.defaultRadius),
                    color = theme.colors.container,
                    onClick = { onOpenWebsite(website) }
                ) {
                    Row(
                        modifier = Modifier.padding(theme.dimension.mediumSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, modifier = Modifier.size(theme.dimension.smallIconSize))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = website,
                            style = theme.typography.bodySmallMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(theme.dimension.smallIconSize))
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

// ---------- Sample chart ----------
//private fun sampleChart(): List<ChartPoint> {
//    // lightweight stub (replace with real points per selected range)
//    val base = listOf(
//        100.0, 101.2, 100.9, 103.5, 102.8, 104.1, 103.6, 105.2, 104.7, 106.3,
//        107.1, 106.4, 108.2, 109.0, 108.6, 110.3, 109.7, 111.2, 110.6, 112.0
//    )
//    return base.mapIndexed { i, c ->
//        ChartPoint(
//            date = "2025-01-${(i + 1).toString().padStart(2, '0')}",
//            open = c - 0.6,
//            low = c - 1.1,
//            high = c + 0.9,
//            close = c,
//            volume = 1_000_000L + (i * 25_000L)
//        )
//    }
//}