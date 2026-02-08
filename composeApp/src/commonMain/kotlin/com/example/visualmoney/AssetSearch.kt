package com.example.visualmoney

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.visualmoney.assetDetails.AssetLogoContainer
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.logoUrl
import com.example.visualmoney.home.ChipContainer
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.theme
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.chevron_right
import visualmoney.composeapp.generated.resources.edit_variant
import visualmoney.composeapp.generated.resources.search


// ---------- Models ----------
enum class AssetCategory(val label: String) {
    STOCKS("Security"), CRYPTO("Crypto"), COMMODITIES("Commodities"), OTHER("Other")
}

val AssetCategory.exchangeName: String
    get() = when (this) {
        AssetCategory.CRYPTO -> "CRYPTO"
        AssetCategory.COMMODITIES -> "COMMODITY"
        else -> ""
    }

enum class AssetType(val label: String) { LISTED("Listed Asset"), UNLISTED("Unlisted Asset") }
enum class SortMode(val label: String) { TRENDING("Trending"), PRICE("Price"), CHANGE("Change") }


data class SearchResultRowUi(
    val symbol: String,
    val name: String,
    val priceText: String = "",
    val changePct: Double = 0.0, // e.g. -0.08
    val assetType: AssetCategory,
    val iconUrl: String? = null,
    val exchangeName: String = "", // e.g. -0.08
)

val PortfolioAsset.toSearchResultRowUi: SearchResultRowUi
    get() = SearchResultRowUi(
        symbol = symbol,
        name = name,
        exchangeName = exchangeName,
        iconUrl = logoUrl,
        assetType = type)

// ---------- Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSearchScreen(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    title: String = "Explore investments",
    initialTab: AssetCategory = AssetCategory.STOCKS,
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
            SearchBar(query = query, onQueryChange = { query = it })
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
    modifier: Modifier = Modifier,
    query: String,
    placeholder: String = "",
    onQueryChange: (String) -> Unit,
) {
//    GlassCard() {
    Row(
        modifier = modifier.fillMaxWidth()
            .border(borderStroke, shape = RoundedCornerShape(theme.dimension.defaultRadius))
            .clip(RoundedCornerShape(theme.dimension.defaultRadius)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f)
                .clip(RoundedCornerShape(theme.dimension.defaultRadius)),
            singleLine = true,
            placeholder = {
                Text(
                    placeholder,
                    style = theme.typography.bodyMediumMedium,
                    color = theme.colors.greyTextColor
                )
            },
            leadingIcon = {
                Icon(
                    painterResource(Res.drawable.search),
                    modifier = Modifier.size(theme.dimension.smallIconSize),
                    contentDescription = null,
                    tint = theme.colors.onSurface
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = theme.colors.container,
                unfocusedContainerColor = theme.colors.container,
                disabledContainerColor = theme.colors.container,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = theme.colors.primary.c50

            ),
            textStyle = theme.typography.bodyMedium.copy(color = theme.colors.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)

        )

//        IconWithContainer(
//            onClick = onSortClick,
//            icon = Icons.Rounded.SwapVert,
//            contentDescription = "Sort",
//            containerColor = theme.colors.container
//        )
//        }
    }
}

// ---------- Tabs ----------
@Composable
fun ExploreTabsRow(
    selected: AssetCategory, onSelect: (AssetCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        AssetCategory.entries.forEach { tab ->
            val isSelected = tab == selected
            val bg by animateColorAsState(
                if (isSelected) theme.colors.onSurface else theme.colors.greyScale.c20,
                label = "tabBg"
            )
            val border = if (isSelected) Color.Transparent else theme.colors.border
            val textColor = if (isSelected) theme.colors.onPrimary else theme.colors.greyTextColor
            CardContainer(
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
        modifier = Modifier.fillMaxWidth()
            .border(borderStroke, shape = RoundedCornerShape(theme.dimension.defaultRadius)),
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
            val textColor =
                if (isSelected) theme.colors.onPrimary else theme.colors.greyTextColor.copy(alpha = 0.7f)
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
    compact: Boolean = false,
    onClick: () -> Unit = {},

    ) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(theme.dimension.largeSpacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        // Logo placeholder
        AssetLogoContainer(item.iconUrl, item.symbol)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
        ) {
            Text(
                text = item.name,
                style = theme.typography.bodyMediumMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = theme.colors.onSurface
            )
            Text(
                text = item.symbol,
                style = theme.typography.bodySmall,
                color = theme.colors.greyTextColor
            )
        }

        if (!compact) {
            Column(horizontalAlignment = Alignment.End) {
                ChipContainer(item.exchangeName)
            }
            Icon(
                painterResource(Res.drawable.chevron_right),
                null,
                modifier = Modifier.size(theme.dimension.smallIconSize),
                tint = theme.colors.greyTextColor
            )
        }
    }
}



@Composable
fun SelectedAssetField(modifier: Modifier = Modifier, rowItem: SearchResultRowUi) {
    val boxModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(theme.dimension.defaultRadius))
        .background(theme.colors.surface)
        .border(
            border = borderStroke,
            shape = RoundedCornerShape(theme.dimension.defaultRadius)
        )
        .padding(horizontal = 12.dp, vertical = 10.dp)

    Column(
        modifier =
            modifier
                .fillMaxWidth().padding(bottom = theme.dimension.veryCloseSpacing),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
    ) {
        CardContainer {
            Box(contentAlignment = Alignment.CenterStart, modifier = boxModifier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
                ) {
                    AssetLogoContainer(
                        rowItem.iconUrl,
                        symbol = rowItem.symbol,
                        size = theme.dimension.smallIconSize
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                    ) {
                        Text(
                            rowItem.symbol,
                            style = theme.typography.bodyMediumStrong,
                            color = theme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            rowItem.name,
                            modifier = Modifier.weight(1f),
                            style = theme.typography.bodyMedium,
                            color = theme.colors.greyTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        painterResource(Res.drawable.edit_variant),
                        null,
                        tint = theme.colors.greyTextColor,
                        modifier = Modifier.size(theme.dimension.smallIconSize),)


                }

            }
        }

    }
}

// ---------- Sample data ----------
fun sampleSearchResults() = listOf(
    SearchResultRowUi("RHM", "Rheinmetall", "1,828.50 €", -0.08, AssetCategory.STOCKS),
    SearchResultRowUi("NVDA", "NVIDIA", "158.62 €", -0.09, AssetCategory.STOCKS),
    SearchResultRowUi("PLTR", "Palantir Technologies", "143.16 €", -0.25, AssetCategory.STOCKS),
    SearchResultRowUi("TSLA", "Tesla", "379.80 €", -0.06, AssetCategory.STOCKS),
    SearchResultRowUi("VLA", "Valneva", "4.12 €", -0.48, AssetCategory.STOCKS),
    SearchResultRowUi("AAPL", "Apple", "209.50 €", -0.14, AssetCategory.STOCKS),

)