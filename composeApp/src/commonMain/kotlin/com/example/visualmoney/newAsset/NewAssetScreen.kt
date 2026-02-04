package com.example.visualmoney.newAsset

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.input.KeyboardType
import com.example.visualmoney.AssetType
import com.example.visualmoney.AssetTypeSelector
import com.example.visualmoney.ExploreTab
import com.example.visualmoney.LocalCountries
import com.example.visualmoney.SearchBar
import com.example.visualmoney.SearchResultRow
import com.example.visualmoney.SortMode
import com.example.visualmoney.calendar.now
import com.example.visualmoney.core.Country
import com.example.visualmoney.core.DateInputTextField
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.GlassCard
import com.example.visualmoney.home.format
import com.example.visualmoney.home.theme
import com.example.visualmoney.newAsset.event.ListedAssetInputEvent
import com.example.visualmoney.newAsset.event.ManualAssetInputEvent
import com.example.visualmoney.newAsset.state.ListedAssetInputState
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAssetScreen(
    sheetState: SheetState,
    viewModel: NewAssetViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(AssetType.LISTED) }
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = { onBack() },
        dragHandle = {},
        containerColor = theme.colors.surface,
    ) {
        Surface(color = theme.colors.surface) {
            Box(
                modifier = Modifier.fillMaxSize().padding(theme.dimension.pagePadding),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
                ) {
                    TopNavigationBar(
                        title = "New Asset",
                        subtitle = "Add a new asset to your portfolio",
                        onBack = onBack
                    )
                    AssetTypeSelector(selected = selectedTab, onSelect = {
                        selectedTab = it
                    })
                    AnimatedContent(selectedTab) { tab ->
                        when (tab) {
                            AssetType.LISTED -> SearchStocksScreen(
                                state = viewModel.listedAssetInputState,
                                onEvent = { viewModel.onListedAssetInputEvent(it) })

                            AssetType.UNLISTED -> ManualAssetInputScreen(
                                state = viewModel.manualAssetInputState,
                                onEvent = { viewModel.onFixedAssetInputEvent(it) })
                        }
                    }
                    Spacer(modifier = Modifier.height(theme.dimension.veryLargeSpacing * 3))
                }
//                LargeButton(
//                    modifier = Modifier.padding(bottom = theme.dimension.veryLargeSpacing),
//                    text = "Save asset",
//                    iconVector = Icons.Rounded.Check,
//                    iconPosition = IconPosition.TRAILING,
//                    onClick = {},
//                )

            }
        }

    }
}

data class ManualAssetInputState(
    val name: String = "",

    // Keep TextField-friendly values as Strings
    val quantityText: String = "1", val unitPriceText: String = "",

    val purchaseDate: LocalDate = LocalDate.now(),

    // Optional MVP fields that enable future diversification analysis
    val country: Country = Country("", ""), // e.g. "US", "ES" (optional)
    val sector: String = "",  // e.g. "Real Estate" (optional)

    // Derived (computed) values for UI display / save button enablement
    val computedTotalValue: Double = 0.0, val canSubmit: Boolean = false, val error: String? = null

)

@Composable
fun ManualAssetInputScreen(
    modifier: Modifier = Modifier,
    state: ManualAssetInputState = ManualAssetInputState(),
    onEvent: (ManualAssetInputEvent) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        InputTextField(
            label = "Name",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            value = state.name,
            onValueChange = { onEvent(ManualAssetInputEvent.NameChanged(it)) })

        Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
            InputTextField(
                modifier = Modifier.weight(1f),
                label = "Quantity",
                value = state.quantityText,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { onEvent(ManualAssetInputEvent.QuantityChanged(it)) })

            InputTextField(
                modifier = Modifier.weight(1f),
                label = "Unit price",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.EuroSymbol,
                        contentDescription = null,
                        tint = theme.colors.greyTextColor,
                        modifier = Modifier.size(theme.dimension.smallIconSize)
                    )
                },
                value = state.unitPriceText,
                onValueChange = { onEvent(ManualAssetInputEvent.UnitPriceChanged(it)) })
        }

        // Quick polish: computed total
        InputTextField(
            label = "Total value",
            value = "%.2f".format(state.computedTotalValue),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.EuroSymbol,
                    contentDescription = null,
                    tint = theme.colors.greyTextColor,
                    modifier = Modifier.size(theme.dimension.smallIconSize)
                )
            },
            onValueChange = {})
        Row(
            modifier = Modifier.fillMaxWidth().clickable {
                expanded = true
            },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputTextField(
                readOnly = true,
                label = "Country",
                value = state.country.displayText,
                placeholder = "Choose a country..",
                trailingIcon = {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = theme.colors.greyTextColor,
                        modifier = Modifier.size(theme.dimension.smallIconSize)
                    )
                })
            DropdownMenu(
                expanded = expanded,
                containerColor = theme.colors.surface,
                shape = RoundedCornerShape(theme.dimension.defaultRadius),
                onDismissRequest = { expanded = false },
            ) {
                LocalCountries.current.forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code.displayText, style = theme.typography.bodySmallMedium) },
                        onClick = {
                            onEvent(ManualAssetInputEvent.CountryChanged(code))
                            expanded = false
                        })
                }
            }

        }

        DateInputTextField(
            label = "Purchase date",
            value = state.purchaseDate,
            onValueChange = { onEvent(ManualAssetInputEvent.PurchaseDateChanged(it)) })

        // Optional MVP fields (can hide behind "Advanced" expand later)
        // InputTextField(label="Country", value=state.country, onValueChange={ onEvent(FixedAssetInputEvent.CountryChanged(it)) })
        // InputTextField(label="Sector", value=state.sector, onValueChange={ onEvent(FixedAssetInputEvent.SectorChanged(it)) })

        // Show validation feedback (simple MVP)
        state.error?.let { err ->
            Text(text = err, color = theme.colors.error)
        }


    }
}

@Composable
fun SearchStocksScreen(
    state: ListedAssetInputState, onEvent: (ListedAssetInputEvent) -> Unit = {}
) = with(state) {
    var sortMode by remember { mutableStateOf(SortMode.TRENDING) }

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
    ) {
        SecondaryTabRow(
            selectedTabIndex = currentTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            contentColor = theme.colors.onSurface,
            containerColor = theme.colors.surface,
            divider = { ListDivider() },
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(currentTab.ordinal, matchContentSize = false)
                        .clip(RoundedCornerShape(theme.dimension.verySmallRadius)),
                    color = theme.colors.primary.c50,
                )
            }
        ) {
            ExploreTab.entries.forEach { currentTab ->
                Tab(selected = currentTab == currentTab, onClick = {
                    onEvent(ListedAssetInputEvent.SectionSelected(currentTab))
                }) {
                    Row(
                        modifier = Modifier.padding(vertical = theme.dimension.mediumSpacing),
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentTab.label,
                            style = theme.typography.bodyMediumMedium
                        )
                    }
                }
            }
        }
        SearchBar(
            query = query,
            onQueryChange = { onEvent(ListedAssetInputEvent.QueryChanged(it)) },
            onSortClick = {
                sortMode = when (sortMode) {
                    SortMode.TRENDING -> SortMode.PRICE
                    SortMode.PRICE -> SortMode.CHANGE
                    SortMode.CHANGE -> SortMode.TRENDING
                }
            })
        Text(
            text = "Showing ${results.size} results",
            style = theme.typography.bodySmall,
            color = theme.colors.onSurface
        )
        GlassCard {
            LazyColumn(
                modifier = Modifier
            ) {
                itemsIndexed(results) { idx, row ->
                    SearchResultRow(
                        item = row, onClick = {
                            onEvent(ListedAssetInputEvent.SymbolSelected(row.symbol))
                        })
                    if (idx != results.lastIndex) {
                        ListDivider()
                    }
                }
            }
        }
    }

}

@Composable
fun SearchCryptoScreen(
    state: ListedAssetInputState, onEvent: (ListedAssetInputEvent) -> Unit = {}
) = with(state) {
    var selectedTab by remember { mutableStateOf(ExploreTab.STOCKS) }
    var sortMode by remember { mutableStateOf(SortMode.TRENDING) }
    var regionSelected by remember { mutableStateOf(false) }
    var industrySelected by remember { mutableStateOf(false) }
    var localQuery by mutableStateOf("")


    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
    ) {

        SearchBar(
            query = query,
            onQueryChange = { onEvent(ListedAssetInputEvent.QueryChanged(it)) },
            onSortClick = {
                sortMode = when (sortMode) {
                    SortMode.TRENDING -> SortMode.PRICE
                    SortMode.PRICE -> SortMode.CHANGE
                    SortMode.CHANGE -> SortMode.TRENDING
                }
            })
        Text(
            text = "Showing ${results.size} results",
            style = theme.typography.bodySmall,
            color = theme.colors.onSurface
        )
        GlassCard {
            LazyColumn(
                modifier = Modifier
            ) {
                itemsIndexed(results) { idx, row ->
                    SearchResultRow(
                        item = row, onClick = {
                            onEvent(ListedAssetInputEvent.SymbolSelected(row.symbol))
                        })
                    if (idx != results.lastIndex) {
                        ListDivider()
                    }
                }
            }
        }
    }

}