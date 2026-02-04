package com.example.visualmoney

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.domain.model.Asset
import com.example.visualmoney.home.GlassCard
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.format
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.search
import kotlin.math.abs

private val theme @Composable get() = LocalAppTheme.current

// ---------- Models ----------
enum class ExploreTab(val label: String) {
    STOCKS("Stocks"), CRYPTO("Crypto"), ETF("ETFs"), FUNDS("Funds")
}

enum class AssetType(val label: String) { LISTED("Listed Asset"), UNLISTED("Unlisted Asset") }
enum class SortMode(val label: String) { TRENDING("Trending"), PRICE("Price"), CHANGE("Change") }


data class SearchResultRowUi(
    val symbol: String,
    val name: String,
    val priceText: String,
    val changePct: Double = 0.0, // e.g. -0.08
    val assetType: ExploreTab,
    val iconUrl: String? = null,
    val exchangeName: String = "", // e.g. -0.08
)

// ---------- Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSearchScreen(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    title: String = "Explore investments",
    initialTab: ExploreTab = ExploreTab.STOCKS,
    resultsCount: Int? = null,
    results: List<SearchResultRowUi> = sampleSearchResults(),
    onBack: () -> Unit = {},
    onOpenFilters: () -> Unit = {},
    onResultClick: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(initialTab) }
    var sortMode by remember { mutableStateOf(SortMode.TRENDING) }
    var regionSelected by remember { mutableStateOf(false) }
    var industrySelected by remember { mutableStateOf(false) }

    val filtered = remember(query, selectedTab, results) {
        results.filter { it.assetType == selectedTab }.filter {
            if (query.isBlank()) true
            else (it.name.contains(query, ignoreCase = true) || it.symbol.contains(
                query,
                ignoreCase = true
            ))
        }
    }

    val sorted = remember(filtered, sortMode) {
        when (sortMode) {
            SortMode.TRENDING -> filtered
            SortMode.PRICE -> filtered // (keep as-is; you can sort by numeric price if you have it)
            SortMode.CHANGE -> filtered.sortedByDescending { it.changePct }
        }
    }
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = { onBack() },
        dragHandle = {},
        containerColor = theme.colors.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
        ) {
            TopNavigationBar(
                title = title, onBack = onBack
            )
            SearchBar(query = query, onQueryChange = { query = it }, onSortClick = {
                sortMode = when (sortMode) {
                    SortMode.TRENDING -> SortMode.PRICE
                    SortMode.PRICE -> SortMode.CHANGE
                    SortMode.CHANGE -> SortMode.TRENDING
                }
            })
            ExploreTabsRow(
                selected = selectedTab, onSelect = { selectedTab = it })
            FiltersRow(
                regionSelected = regionSelected,
                industrySelected = industrySelected,
                onToggleRegion = { regionSelected = !regionSelected; onOpenFilters() },
                onToggleIndustry = { industrySelected = !industrySelected; onOpenFilters() })
            Text(
                text = "Showing ${resultsCount ?: sorted.size} results",
                style = theme.typography.bodySmall,
                color = theme.colors.onSurface
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().border(
                    1.dp, theme.colors.border, RoundedCornerShape(theme.dimension.defaultRadius)
                ).clip(RoundedCornerShape(theme.dimension.defaultRadius))
                    .background(theme.colors.surface)
            ) {
                itemsIndexed(sorted) { idx, row ->
                    SearchResultRow(
                        item = row, onClick = { onResultClick(row.symbol) })
                    if (idx != sorted.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = theme.colors.border,
                            modifier = Modifier.padding(horizontal = theme.dimension.largeSpacing)
                        )
                    }
                }
            }
        }
    }
}


// ---------- Search bar ----------
@OptIn(FlowPreview::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSortClick: () -> Unit,
) {
    GlassCard() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(theme.dimension.defaultRadius)).border(borderStroke),
                singleLine = true,
                placeholder = {
                    Text(
                        "Search ticker…",
                        style = theme.typography.bodySmall,
                        color = theme.colors.greyTextColor
                    )
                },
                leadingIcon = {
                    Icon(
                        painterResource(Res.drawable.search),
                        modifier = Modifier.size(theme.dimension.smallIconSize),
                        contentDescription = null
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = theme.colors.onPrimary,
                    unfocusedContainerColor = theme.colors.onPrimary,
                    disabledContainerColor = theme.colors.onPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = theme.typography.bodySmallMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)

            )

//        IconWithContainer(
//            onClick = onSortClick,
//            icon = Icons.Rounded.SwapVert,
//            contentDescription = "Sort",
//            containerColor = theme.colors.container
//        )
        }
    }
}

// ---------- Tabs ----------
@Composable
fun ExploreTabsRow(
    selected: ExploreTab, onSelect: (ExploreTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        ExploreTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val bg by animateColorAsState(
                if (isSelected) theme.colors.onSurface else theme.colors.greyScale.c20,
                label = "tabBg"
            )
            val border = if (isSelected) Color.Transparent else theme.colors.border
            val textColor = if (isSelected) theme.colors.onPrimary else theme.colors.greyTextColor
            GlassCard(
                modifier = Modifier.weight(1f),
                containerColor = bg
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(tab) }
                        .padding(vertical = theme.dimension.largeSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = theme.typography.bodyMediumStrong,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun AssetTypeSelector(
    selected: AssetType, onSelect: (AssetType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().border(borderStroke, shape = RoundedCornerShape(theme.dimension.defaultRadius)),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        AssetType.entries.forEach { tab ->
            val isSelected = tab == selected
            val bg by animateColorAsState(
                if (isSelected) theme.colors.primary.c50 else theme.colors.greyScale.c20,
                label = "tabBg"
            )
            val border = if (isSelected) Color.Transparent else theme.colors.border
            val shape = if (tab.ordinal == 0) RoundedCornerShape(
                topStart = theme.dimension.defaultRadius,
                bottomStart = theme.dimension.defaultRadius
            ) else RoundedCornerShape(
                topEnd = theme.dimension.defaultRadius,
                bottomEnd = theme.dimension.defaultRadius
            )
            val textColor = if (isSelected) theme.colors.onSurface else theme.colors.greyTextColor.copy(alpha = 0.7f)
            Surface(
                modifier = Modifier.weight(1f),
                color = bg,
                shape = shape
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(tab) }
                        .padding(vertical = theme.dimension.mediumSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = theme.typography.bodyMediumStrong,
                        color = textColor
                    )
                }

            }
        }
    }
}

// ---------- Filters ----------
@Composable
fun FiltersRow(
    regionSelected: Boolean,
    industrySelected: Boolean,
    onToggleRegion: () -> Unit,
    onToggleIndustry: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        FilterChip(
            text = "Region",
            selected = regionSelected,
            icon = Icons.Outlined.FilterAlt,
            onClick = onToggleRegion
        )
        FilterChip(
            text = "Industry",
            selected = industrySelected,
            icon = Icons.Outlined.FilterAlt,
            onClick = onToggleIndustry
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) theme.colors.container else theme.colors.surface, label = "chipBg"
    )

    Surface(
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        color = bg,
        border = BorderStroke(1.dp, theme.colors.border),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(theme.dimension.smallIconSize)
            )
            Text(text, style = theme.typography.bodySmallMedium)
        }
    }
}

// ---------- Row item ----------
@Composable
fun SearchResultRow(
    item: SearchResultRowUi,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(theme.dimension.largeSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        // Logo placeholder
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(theme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            item.iconUrl?.let {
                AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop)
            } ?: Text(
                text = item.symbol.take(1),
                style = theme.typography.bodyMediumMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
        ) {
            Text(
                text = item.name,
                style = theme.typography.bodyMediumMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.symbol,
                style = theme.typography.bodySmall,
                color = theme.colors.greyScale.c60
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.exchangeName, style = theme.typography.bodyMediumMedium
            )
        }
    }
}

// ---------- Small shared icon container (same look as your Home) ----------
@Composable
private fun IconWithContainer(
    onClick: () -> Unit = {},
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String = "",
    containerColor: Color = theme.colors.container,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape
) {
    Box(
        modifier = modifier.clip(shape).background(containerColor).clickable { onClick() }) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(theme.dimension.mediumSpacing)
                .size(theme.dimension.iconSize),
            tint = theme.colors.onSurface
        )
    }
}

// ---------- Sample data ----------
fun sampleSearchResults() = listOf(
    SearchResultRowUi("RHM", "Rheinmetall", "1,828.50 €", -0.08, ExploreTab.STOCKS),
    SearchResultRowUi("NVDA", "NVIDIA", "158.62 €", -0.09, ExploreTab.STOCKS),
    SearchResultRowUi("PLTR", "Palantir Technologies", "143.16 €", -0.25, ExploreTab.STOCKS),
    SearchResultRowUi("TSLA", "Tesla", "379.80 €", -0.06, ExploreTab.STOCKS),
    SearchResultRowUi("VLA", "Valneva", "4.12 €", -0.48, ExploreTab.STOCKS),
    SearchResultRowUi("AAPL", "Apple", "209.50 €", -0.14, ExploreTab.STOCKS),

    SearchResultRowUi("SPY", "SPDR S&P 500 ETF", "506.11 $", +0.12, ExploreTab.FUNDS),
    SearchResultRowUi("BTC", "Bitcoin", "43,210 $", +1.35, ExploreTab.CRYPTO),
    SearchResultRowUi("VTI", "Vanguard Total Stock Market", "255.44 $", -0.05, ExploreTab.FUNDS),
)