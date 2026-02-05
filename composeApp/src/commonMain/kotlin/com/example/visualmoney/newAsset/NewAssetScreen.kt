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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.visualmoney.AssetType
import com.example.visualmoney.AssetTypeSelector
import com.example.visualmoney.ExploreTab
import com.example.visualmoney.SearchBar
import com.example.visualmoney.SearchResultRow
import com.example.visualmoney.SortMode
import com.example.visualmoney.calendar.now
import com.example.visualmoney.core.Country
import com.example.visualmoney.core.DateInputTextField
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.core.dismissKeyboardOnScroll
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
    viewModel: NewAssetViewModel,
    onBack: () -> Unit = {},
    onNavigateToAssetDetails: (String) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(AssetType.LISTED) }
    Scaffold(containerColor = Color.Transparent) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
        ) {
            TopNavigationBar(
                title = "New Asset",
                subtitle = "Add a new asset to your portfolio",
                onBack = onBack
            )
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = theme.dimension.pagePadding),
                contentAlignment = Alignment.TopCenter
            ) {
                SearchStocksScreen(
                    state = viewModel.listedAssetInputState,
                    onEvent = { viewModel.onListedAssetInputEvent(it) },
                    onAssetClick = { onNavigateToAssetDetails(it) }
                )
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

data class ManualAssetInputState(
    val name: String = "",

    // Keep TextField-friendly values as Strings
    val quantityText: String = "1", val unitPriceText: String = "",

    val purchaseDate: LocalDate = LocalDate.now(),
    val notes: String = "",
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
        verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
    ) {
        InputTextField(
            label = "Asset name",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            value = state.name,
            onValueChange = { onEvent(ManualAssetInputEvent.NameChanged(it)) })

//        Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
//            InputTextField(
//                modifier = Modifier.weight(1f),
//                label = "Purchase price",
//                value = state.quantityText,
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                onValueChange = { onEvent(ManualAssetInputEvent.QuantityChanged(it)) })
//
//            InputTextField(
//                modifier = Modifier.weight(1f),
//                label = "Unit price",
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//
//                trailingIcon = {
//                    Icon(
//                        imageVector = Icons.Rounded.EuroSymbol,
//                        contentDescription = null,
//                        tint = theme.colors.greyTextColor,
//                        modifier = Modifier.size(theme.dimension.smallIconSize)
//                    )
//                },
//                value = state.unitPriceText,
//                onValueChange = { onEvent(ManualAssetInputEvent.UnitPriceChanged(it)) }
//            )
//        }

        // Quick polish: computed total
        InputTextField(
            label = "Purchase price",
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
//        Row(
//            modifier = Modifier.fillMaxWidth().clickable {
//                expanded = true
//            },
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            InputTextField(
//                readOnly = true,
//                label = "Country",
//                value = state.country.displayText,
//                placeholder = "Choose a country..",
//                trailingIcon = {
//                    Icon(
//                        Icons.Rounded.KeyboardArrowDown,
//                        contentDescription = null,
//                        tint = theme.colors.greyTextColor,
//                        modifier = Modifier.size(theme.dimension.smallIconSize)
//                    )
//                })
//            DropdownMenu(
//                expanded = expanded,
//                containerColor = theme.colors.surface,
//                shape = RoundedCornerShape(theme.dimension.defaultRadius),
//                onDismissRequest = { expanded = false },
//            ) {
//                LocalCountries.current.forEach { code ->
//                    DropdownMenuItem(
//                        text = { Text(code.displayText, style = theme.typography.bodySmallMedium) },
//                        onClick = {
//                            onEvent(ManualAssetInputEvent.CountryChanged(code))
//                            expanded = false
//                        })
//                }
//            }
//
//        }

        DateInputTextField(
            label = "Transaction date",
            value = state.purchaseDate,
            onValueChange = { onEvent(ManualAssetInputEvent.PurchaseDateChanged(it)) })

        InputTextField(
            modifier = Modifier.weight(1f),
            label = "Notes",
            value = state.notes,
            onValueChange = { onEvent(ManualAssetInputEvent.UnitPriceChanged(it)) }
        )

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
    state: ListedAssetInputState,
    onEvent: (ListedAssetInputEvent) -> Unit = {},
    onAssetClick: (String) -> Unit = {},
) = with(state) {
    var sortMode by remember { mutableStateOf(SortMode.TRENDING) }
    val scrollState = rememberScrollState(1)
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val localController = LocalSoftwareKeyboardController.current
    var showOtherView by remember { mutableStateOf(false) }
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            localController?.hide()
            focusManager.clearFocus(force = true)
        }
    }
    LaunchedEffect(state.currentTab) {
        if (state.currentTab == ExploreTab.OTHER) {
            showOtherView = true
        } else {
            showOtherView = false
        }
    }
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            contentColor = theme.colors.onSurface,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = { ListDivider() },
            scrollState = scrollState,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(currentTab.ordinal, matchContentSize = false)
                        .clip(RoundedCornerShape(theme.dimension.verySmallRadius)),
                    color = theme.colors.primary.c50,
                )
            }
        ) {
            ExploreTab.entries.forEach { currentTab ->
                Tab(
                    modifier = Modifier.clip(
                        RoundedCornerShape(
                            topStart = theme.dimension.defaultRadius,
                            topEnd = theme.dimension.defaultRadius
                        )
                    ),
                    selected = currentTab == currentTab,
                    onClick = {
                        onEvent(ListedAssetInputEvent.SectionSelected(currentTab))
                    }) {
                    Row(
                        modifier = Modifier.padding(vertical = theme.dimension.mediumSpacing),
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

        AnimatedContent(showOtherView) { show ->
            if (show) {
                ManualAssetInputScreen()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)) {
                    SearchBar(
                        query = query,
                        placeholder = state.searchBarPlaceHolder,
                        onQueryChange = { onEvent(ListedAssetInputEvent.QueryChanged(it)) },
                        onSortClick = {
                            sortMode = when (sortMode) {
                                SortMode.TRENDING -> SortMode.PRICE
                                SortMode.PRICE -> SortMode.CHANGE
                                SortMode.CHANGE -> SortMode.TRENDING
                            }
                        }
                    )
                    Text(
                        text = "Showing ${results.size} results",
                        style = theme.typography.bodySmall,
                        color = theme.colors.onSurface
                    )
                    GlassCard {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier,

                            ) {
                            itemsIndexed(results) { idx, row ->
                                SearchResultRow(
                                    item = row, onClick = {
                                        onEvent(ListedAssetInputEvent.SymbolSelected(row.symbol))
                                        onAssetClick(row.symbol)
                                    })
                                if (idx != results.lastIndex) {
                                    ListDivider(modifier = Modifier.padding(horizontal = theme.dimension.pagePadding))
                                }
                            }
                        }
                    }
                }
            }

        }


    }

}

