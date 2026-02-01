package com.example.visualmoney.newAsset

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.visualmoney.ExploreTab
import com.example.visualmoney.ExploreTabsRow
import com.example.visualmoney.SearchBar
import com.example.visualmoney.SearchResultRow
import com.example.visualmoney.SearchResultRowUi
import com.example.visualmoney.SortMode
import com.example.visualmoney.calendar.now
import com.example.visualmoney.core.Country
import com.example.visualmoney.core.DateInputTextField
import com.example.visualmoney.core.IconPosition
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.LargeButton
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.core.getCountries
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.GlassCard
import com.example.visualmoney.home.format
import com.example.visualmoney.home.theme
import com.example.visualmoney.newAsset.event.ManualAssetInputEvent
import com.example.visualmoney.sampleSearchResults
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAssetScreen(
    sheetState: SheetState,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(ExploreTab.STOCKS) }
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = { onBack() },
        dragHandle = {},
        containerColor = theme.colors.surface,
    ) {
        Surface(color = theme.colors.surface) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(theme.dimension.pagePadding),
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
                    ExploreTabsRow(
                        selected = selectedTab,
                        onSelect = { selectedTab = it }
                    )
                    AnimatedContent(selectedTab) { tab ->
                        when (tab) {
                            ExploreTab.STOCKS -> SearchStocksScreen()
                            ExploreTab.CRYPTO -> Surface { }
                            ExploreTab.FIXED -> ManualAssetInputScreen()
                        }
                    }
                    Spacer(modifier = Modifier.height(theme.dimension.veryLargeSpacing * 3))
                }
                LargeButton(
                    modifier = Modifier.padding(bottom = theme.dimension.veryLargeSpacing),
                    text = "Save asset",
                    iconVector = Icons.Rounded.Check,
                    iconPosition = IconPosition.TRAILING,
                    onClick = {},
                )

            }
        }

    }
}

data class ManualAssetInputState(
    val name: String = "",

    // Keep TextField-friendly values as Strings
    val quantityText: String = "1",
    val unitPriceText: String = "",

    val purchaseDate: LocalDate = LocalDate.now(),

    // Optional MVP fields that enable future diversification analysis
    val country: Country = Country("",""), // e.g. "US", "ES" (optional)
    val sector: String = "",  // e.g. "Real Estate" (optional)

    // Derived (computed) values for UI display / save button enablement
    val computedTotalValue: Double = 0.0,
    val canSubmit: Boolean = false,
    val error: String? = null

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
            onValueChange = { onEvent(ManualAssetInputEvent.NameChanged(it)) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
            InputTextField(
                modifier = Modifier.weight(1f),
                label = "Quantity",
                value = state.quantityText,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { onEvent(ManualAssetInputEvent.QuantityChanged(it)) }
            )

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
                onValueChange = { onEvent(ManualAssetInputEvent.UnitPriceChanged(it)) }
            )
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
            onValueChange = {}
        )
        Row(modifier = Modifier.clickable {
             expanded = true
        }) {
            InputTextField(
                readOnly = true,
                label = "Country",
                value = state.country.displayText
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                getCountries().forEach { code ->
                    DropdownMenuItem(
                        text = { Text(code.displayText) },
                        onClick = {
//                        onPhoneNumberChange(
//                            phoneNumber.copy(countryCode = code)
//                        )
                            expanded = false
                        }
                    )
                }
            }

        }

        DateInputTextField(
            label = "Purchase date",
            value = state.purchaseDate,
            onValueChange = { onEvent(ManualAssetInputEvent.PurchaseDateChanged(it)) }
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
    results: List<SearchResultRowUi> = sampleSearchResults(),
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ExploreTab.STOCKS) }
    var sortMode by remember { mutableStateOf(SortMode.TRENDING) }
    var regionSelected by remember { mutableStateOf(false) }
    var industrySelected by remember { mutableStateOf(false) }

    val filtered = remember(query, selectedTab, results) {
        results
            .filter { it.assetType == selectedTab }
            .filter {
                if (query.isBlank()) true
                else (it.name.contains(query, ignoreCase = true)
                        || it.symbol.contains(query, ignoreCase = true))
            }
    }

    val sorted = remember(filtered, sortMode) {
        when (sortMode) {
            SortMode.TRENDING -> filtered
            SortMode.PRICE -> filtered // (keep as-is; you can sort by numeric price if you have it)
            SortMode.CHANGE -> filtered.sortedByDescending { it.changePct }
        }
    }
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
    ) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSortClick = {
                sortMode = when (sortMode) {
                    SortMode.TRENDING -> SortMode.PRICE
                    SortMode.PRICE -> SortMode.CHANGE
                    SortMode.CHANGE -> SortMode.TRENDING
                }
            }
        )
        Text(
            text = "Showing ${sorted.size} results",
            style = theme.typography.bodySmall,
            color = theme.colors.onSurface
        )
        GlassCard {
            LazyColumn(
                modifier = Modifier
            ) {
                itemsIndexed(sorted) { idx, row ->
                    SearchResultRow(
                        item = row,
                        onClick = {

                        }
                    )
                    if (idx != sorted.lastIndex) {
                        ListDivider()
                    }
                }
            }
        }
    }

}