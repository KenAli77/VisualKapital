package com.example.visualmoney.newAsset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.visualmoney.ExploreTab
import com.example.visualmoney.ExploreTabsRow
import com.example.visualmoney.core.IconPosition
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.LargeButton
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.home.theme

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
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
                    .padding(theme.dimension.pagePadding)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
                ) {
                    TopNavigationBar(
                        title = "New Asset",
                        subtitle = "Add a new asset to your portfolio",
                        onBack = onBack
                    )

//                AnimatedContent(selectedTab) { tab ->
//                    when (tab) {
//                        ExploreTab.STOCKS -> TODO()
//                        ExploreTab.ETFS -> TODO()
//                        ExploreTab.CRYPTO -> TODO()
//                        ExploreTab.FUNDS -> TODO()
//                    }
//                }

                    ExploreTabsRow(
                        selected = selectedTab,
                        onSelect = { selectedTab = it }
                    )



                    Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                        InputTextField(
                            label = "Name",
                            value = "",
                            onValueChange = {}
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                            InputTextField(
                                modifier = Modifier.weight(1f),
                                label = "Quantity"
                            )
                            InputTextField(
                                modifier = Modifier.weight(1f),
                                label = "Price"
                            )
                        }
                        InputTextField(
                            label = "Purchase date"
                        )

                    }
                }
                LargeButton(
                    text = "Save asset",
                    iconVector = Icons.Rounded.Check,
                    iconPosition = IconPosition.TRAILING,

                    onClick = {},
                )
                Spacer(modifier = Modifier.height(theme.dimension.veryLargeSpacing * 8))

            }
        }

    }
}