package com.visualmoney.app.newAsset

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.visualmoney.app.AssetCategory
import com.visualmoney.app.SearchBar
import com.visualmoney.app.SearchResultRow
import com.visualmoney.app.SelectedAssetField
import com.visualmoney.app.calendar.now
import com.visualmoney.app.core.DateInputTextField
import com.visualmoney.app.core.InputTextField
import com.visualmoney.app.core.LargeButton
import com.visualmoney.app.core.ListDivider
import com.visualmoney.app.core.TopNavigationBar
import com.visualmoney.app.greyTextColor
import com.visualmoney.app.home.CardContainer
import com.visualmoney.app.home.IconWithContainer
import com.visualmoney.app.home.format
import com.visualmoney.app.home.theme
import com.visualmoney.app.newAsset.event.AssetInputEvent
import com.visualmoney.app.newAsset.event.ManualAssetInputEvent
import com.visualmoney.app.newAsset.state.AssetInputState
import com.visualmoney.app.newAsset.state.isOtherTab
import com.visualmoney.app.newAsset.state.isValidForSubmit
import com.visualmoney.app.newAsset.state.totalValue
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.close
import visualmoney.composeapp.generated.resources.search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAssetScreen(
    modifier: Modifier = Modifier,
    viewModel: NewAssetViewModel,
    onBack: () -> Unit = {},
) = with(viewModel) {
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event) {
                onBack()
            }
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
        ) {
            Row(modifier = Modifier.padding(horizontal = theme.dimension.pagePadding)) {
                TopNavigationBar(
                    title = "New Asset",
                    subtitle = "Add a new asset to your portfolio",
                    onBack = onBack
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
            ) {
                NewAssetInputContent(
                    modifier = Modifier.padding(horizontal = theme.dimension.pagePadding),
                    state = viewModel.listedAssetInputStateState,
                    onEvent = { viewModel.onListedAssetInputEvent(it) },
                )
                Column(
                    modifier = Modifier.fillMaxWidth().background(theme.colors.surface)
                ) {
                    ListDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = theme.dimension.pagePadding)
                            .padding(vertical = theme.dimension.veryLargeSpacing),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total amount",
                            style = theme.typography.bodyLarge,
                            color = theme.colors.onSurface
                        )
                        Text(
                            color = theme.colors.onSurface,
                            text = "%.2f".format(listedAssetInputStateState.totalValue),
                            style = theme.typography.titleSmallMedium
                        )
                    }
                    LargeButton(
                        modifier = Modifier.padding(horizontal = theme.dimension.pagePadding),
                        text = "Add to portfolio",
                        enabled = listedAssetInputStateState.isValidForSubmit,
                        onClick = {
                            onListedAssetInputEvent(AssetInputEvent.Submit)
                        }
                    )
                    Spacer(modifier = Modifier.height(theme.dimension.bottomBarHeight * 2))
                }

            }

        }

    }
}

data class ManualAssetInputState(
    val name: String = "",
    // Keep TextField-friendly values as Strings
    val quantityText: String = "1", val unitPriceText: String = "",
    val purchaseDate: LocalDate = LocalDate.now(), val notes: String = "",
    // Derived (computed) values for UI display / save button enablement
    val computedTotalValue: Double = 0.0, val canSubmit: Boolean = false, val error: String? = null

)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetInputScreen(
    modifier: Modifier = Modifier,
    state: AssetInputState,
    onEvent: (AssetInputEvent) -> Unit = {}
) = with(state) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSearchSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    val localController = LocalSoftwareKeyboardController.current
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            localController?.hide()
            focusManager.clearFocus(force = true)
        }
    }
    Column(
        modifier = modifier,
    ) {
        if (showSearchSheet) {
            ListedAssetSearchSheet(
                state = state, sheetState = sheetState, onEvent = onEvent, onDismiss = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        showSearchSheet = false
                    }
                })
        }
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
            contentPadding = PaddingValues(bottom = theme.dimension.bottomBarHeight * 8)
        ) {
            item() {
                if (state.isOtherTab) {
                    InputTextField(
                        readOnly = false,
                        required = true,
                        label = "Asset name",
                        borderAlwaysVisible = true,
                        placeholder = state.searchBarPlaceHolder,
                        value = state.assetName,
                        onValueChange = {
                            onEvent(AssetInputEvent.NameChanged(it))
                        }
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = theme.dimension.veryCloseSpacing),
                            text = state.assetFieldTitle,
                            style = theme.typography.bodySmallStrong,
                            color = theme.colors.onSurface
                        )
                        Row(modifier = Modifier.clickable {
                            showSearchSheet = true
                        }) {
                            if (selectedSecurity != null) {
                                SelectedAssetField(
                                    rowItem = selectedSecurity,
                                )
                            } else {
                                InputTextField(
                                    readOnly = true,
                                    borderAlwaysVisible = true,
                                    placeholder = state.searchBarPlaceHolder,
                                    leadingIcon = {
                                        Icon(
                                            painterResource(Res.drawable.search),
                                            null,
                                            modifier = Modifier.size(theme.dimension.smallIconSize),
                                            tint = theme.colors.greyTextColor
                                        )
                                    })
                            }
                        }
                    }
                }

            }
            item {
                DateInputTextField(
                    label = "Transaction date",
                    value = state.purchasedAt,
                    onValueChange = { onEvent(AssetInputEvent.PurchaseDateChanged(it)) })
            }
            item {
                InputTextField(
                    label = "Purchase price",
                    required = true,
                    value = state.purchasePrice?.let {
                        it.toString()
                    } ?: "",
                    placeholder = "0.0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.EuroSymbol,
                            contentDescription = null,
                            tint = theme.colors.greyTextColor,
                            modifier = Modifier.size(theme.dimension.smallIconSize)
                        )
                    },
                    onValueChange = {
                        onEvent(AssetInputEvent.PurchasePriceChanged(it.toDoubleOrNull()))
                    })
            }
            item {
                InputTextField(
                    label = "Current value",
                    placeholder = "0.0",
                    required = true,
                    value = currentValue?.let {
                        it.toString()
                    } ?: "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.EuroSymbol,
                            contentDescription = null,
                            tint = theme.colors.greyTextColor,
                            modifier = Modifier.size(theme.dimension.smallIconSize)
                        )
                    },
                    onValueChange = {
                        onEvent(AssetInputEvent.CurrentValueChanged(it.toDoubleOrNull()))
                    })
            }

            item {
                InputTextField(
                    label = "Quantity",
                    required = true,
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    value = "${state.quantity ?: ""}",
                    onValueChange = { onEvent(AssetInputEvent.QtyChanged(it.toIntOrNull())) })
            }
            item {
                InputTextField(
                    label = "Notes",
                    value = state.notes,
                    onValueChange = { onEvent(AssetInputEvent.NotesChanged(it)) })

            }
        }


    }

}


@Composable
fun NewAssetInputContent(
    modifier: Modifier = Modifier,
    state: AssetInputState,
    onEvent: (AssetInputEvent) -> Unit = {},
) = with(state) {
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
        if (state.currentTab == AssetCategory.OTHER) {
            showOtherView = true
        } else {
            showOtherView = false
        }
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                }) {
                AssetCategory.entries.forEach { currentTab ->
                    Tab(
                        modifier = Modifier.clip(
                            RoundedCornerShape(
                                topStart = theme.dimension.defaultRadius,
                                topEnd = theme.dimension.defaultRadius
                            )
                        ), selected = currentTab == currentTab, onClick = {
                            onEvent(AssetInputEvent.SectionSelected(currentTab))
                        }) {
                        Row(
                            modifier = Modifier.padding(vertical = theme.dimension.veryLargeSpacing),
                            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentTab.label, style = theme.typography.bodyMediumStrong
                            )
                        }
                    }
                }
            }
            AssetInputScreen(state = state, onEvent = onEvent)

        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListedAssetSearchSheet(
    sheetState: SheetState,
    state: AssetInputState,
    onEvent: (AssetInputEvent) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) = with(state) {
    val lazyListState = rememberLazyListState()

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = { onDismiss() },
        dragHandle = {},
        containerColor = theme.colors.container,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Search asset",
                        style = theme.typography.titleSmall,
                        color = theme.colors.onSurface
                    )
                    IconWithContainer(
                        icon = painterResource(Res.drawable.close), onClick = onDismiss
                    )
                }
                SearchBar(
                    query = state.query,
                    placeholder = state.searchBarPlaceHolder,
                    onQueryChange = { onEvent(AssetInputEvent.QueryChanged(it)) },
                )
                Text(
                    text = "Showing ${results.size} results",
                    style = theme.typography.bodySmall,
                    color = theme.colors.onSurface
                )
                CardContainer {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier,

                        ) {
                        itemsIndexed(results) { idx, row ->
                            SearchResultRow(
                                item = row, onClick = {
                                    onEvent(AssetInputEvent.SymbolSelected(row.symbol))
                                    onDismiss()
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

