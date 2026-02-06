package com.example.visualmoney.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.visualmoney.DefaultAppColors
import com.example.visualmoney.ExploreSearchScreen
import com.example.visualmoney.GreenGradient
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.assetDetails.AssetLogoContainer
import com.example.visualmoney.core.IconPosition
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.SmallButton
import com.example.visualmoney.greyTextColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.arrow_up_right
import visualmoney.composeapp.generated.resources.calendar
import visualmoney.composeapp.generated.resources.plus
import visualmoney.composeapp.generated.resources.portfolio
import visualmoney.composeapp.generated.resources.trending_up
import visualmoney.composeapp.generated.resources.zigzag
import kotlin.math.round


val theme @Composable get() = LocalAppTheme.current

// ---------- Models ----------
data class HoldingRowUi(
    val symbol: String,
    val name: String,
    val assetClass: AssetClass,
    val changePct: Double,
    val price: Double,
    val dayLow: Double,
    val dayHigh: Double,
    val logoUrl: String,
)

enum class HomeTab(val label: String) {
    Favourites("Top movers"), HotDeals("Upcoming events"), News("Losers"), Gainers("Gainers"), Losers(
        "Losers"
    ),
    H24("24h"),
}

enum class BottomNavItem {
    HOME, STATS, SWAP, ACCOUNT
}

// ---------- Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    userName: String = "James",
    balanceUsd: Double = 5738.25,
    profitUsd: Double = 295.83,
    mlPct: Double = 300.00,
    onGoToCalendar: () -> Unit = {},
    onGoToBalance: () -> Unit = {},
    onNewAsset: () -> Unit = {},
    onGoToAssetDetails: (String) -> Unit = {}
) = with(viewModel) {
    var showSearch by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        if (showSearch) {
            ExploreSearchScreen(sheetState = sheetState, onBack = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showSearch = false
                }
            })
        }


        Column(
            modifier = Modifier.fillMaxSize().padding(theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
        ) {
            HomeTopHeader(userName = userName, onGoToCalendar = onGoToCalendar)
            LazyColumn(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = theme.dimension.defaultRadius,
                        topEnd = theme.dimension.defaultRadius
                    )
                ),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)
            ) {
                item {
                    BalanceCard(
                        balanceUsd = balanceUsd,
                        profitUsd = profitUsd,
                        mlPct = mlPct,
                        onOpen = onGoToBalance,
                        onCurrencyClick = {},
                    )
                }
                item {
                    UnlockPremiumSection(
                        title = "Smarter investing starts here",
                        subtitle = "See risk exposure, portfolio health, and actionable insights. All in one place.",
                        onOpen = {})
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "My portfolio",
                            style = theme.typography.bodyMediumMedium,
                            color = theme.colors.greyTextColor
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Add asset",
                                style = theme.typography.bodyMediumStrong,
                                color = theme.colors.onSurface
                            )
                            IconWithContainer(
                                icon = painterResource(Res.drawable.plus),
                                onClick = onNewAsset
                            )
                        }
                    }
                }
                items(state.holdings) { item ->
                    HoldingRow(
                        modifier = Modifier, item = item, onClick = {
                            onGoToAssetDetails(item.symbol)
                        })
                    ListDivider()
                }

            }

        }
    }
}

// ---------- Header ----------
@Composable
fun HomeTopHeader(
    userName: String, modifier: Modifier = Modifier, onGoToCalendar: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Avatar placeholder
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(theme.colors.surface)
                    .border(1.dp, brush = borderGradient, CircleShape)
            )

            Column() {
                Text(
                    text = "Hello $userName 👋",
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c60
                )
                Text(
                    text = "Welcome Back!",
                    style = theme.typography.bodyMediumMedium,
                    color = theme.colors.onSurface
                )
            }

        }
        Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
            IconWithContainer(
                onClick = onGoToCalendar,
                painterResource(Res.drawable.calendar),
            )
        }


    }
}

@Composable
fun IconWithContainer(
    onClick: () -> Unit = {},
    icon: Painter,
    containerColor: Color = theme.colors.surface,
    modifier: Modifier = Modifier
) {
    CardContainer(
        modifier = modifier.clickable { onClick() },
        containerColor = containerColor,
        shape = CircleShape,
//        onClick = onClick,
//        elevation = CardDefaults.cardElevation(0.dp),
//        border = borderStroke,
//        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = "",
                modifier = Modifier.size(theme.dimension.iconSize),
                tint = theme.colors.onSurface
            )

        }

    }


}

@Composable
fun IconWithContainerSmall(
    onClick: () -> Unit = {},
    icon: Painter,
    contentDescription: String = "",
    containerColor: Color = theme.colors.surface,
    contentColor: Color = theme.colors.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(theme.dimension.smallRadius),
        border = borderStroke,
        color = containerColor,
    ) {
        Box(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(theme.dimension.smallIconSize),
                tint = contentColor
            )

        }
    }


}

@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    containerColor: Color = theme.colors.container,
    shape: Shape = RoundedCornerShape(theme.dimension.defaultRadius),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.shadow(
            elevation = 8.dp,
            shape,
            spotColor = theme.colors.greenScale.c90,
            ambientColor = theme.colors.greenScale.c50
        )
    ) {
        Box(
            modifier = Modifier.matchParentSize()
                .clip(shape)
                .border(borderStroke, shape = shape)
                .blur(radius = 1.dp).padding(1.dp)
                .background(containerColor),
        )
        Column {
            content()
        }

    }
}

// ---------- Balance Card ----------
@Composable
fun BalanceCard(
    balanceUsd: Double,
    profitUsd: Double,
    mlPct: Double,
    onOpen: () -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardContainer(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                    modifier = Modifier
                ) {
                    IconWithContainerSmall(
                        icon = painterResource(Res.drawable.portfolio),
                        contentDescription = "Balance",
                    )
                    Text(
                        text = "Portfolio value",
                        style = theme.typography.bodySmall,
                        color = theme.colors.greyTextColor
                    )


                }
                IconWithContainerSmall(
                    {},
                    icon = painterResource(Res.drawable.arrow_up_right),
                    containerColor = theme.colors.primary.c50,
                    contentColor = theme.colors.onPrimary,
                )

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = "$" + "%.2f".format(balanceUsd)
                    Text(
                        text = text.substringBeforeLast("."),
                        style = theme.typography.titleLarge,
                        color = theme.colors.onSurface
                    )
                    Text(
                        text = "." + text.format(balanceUsd).substringAfterLast("."),
                        style = theme.typography.titleLarge,
                        color = theme.colors.greyScale.c50
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                    modifier = Modifier.clickable {
                        onCurrencyClick()
                    }) {
                    Text(
                        "USD",
                        style = theme.typography.bodySmall,
                        color = theme.colors.greyTextColor
                    )
                    Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Currency",
                        modifier = Modifier.size(theme.dimension.smallIconSize),
                        tint = theme.colors.greyTextColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
                ) {
                    Text(
                        text = "Profit:",
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.greyTextColor
                    )
                    Text(
                        text = "$${"%.2f".format(profitUsd)}",
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
                ) {
                    Text(
                        text = "ML:",
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.greyTextColor
                    )
                    Text(
                        text = "${"%.2f".format(mlPct)}%",
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.onSurface
                    )

                }
            }
        }
    }
}


@Composable
fun QuickActionButton(
    icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = CircleShape,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, brush = borderGradient),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.onSurface)
    ) {
        Box(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = "",
                modifier = Modifier.size(theme.dimension.iconSize),
                tint = theme.colors.onPrimary
            )
        }

    }

}

val primaryGradient = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFBC42), 0.5f to Color(0xFFFFC653), 1.0f to Color(0xFFFFA904)
    ), start = Offset(0f, 0f), end = Offset.Infinite
)


val borderGradient = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to DefaultAppColors.greyScale.c80.copy(alpha = 1f),
//        0.25f to Color(0xFFFFFFFF).copy(alpha = 0.5f),
        0.5f to DefaultAppColors.greyScale.c80.copy(alpha = 0f),
//        0.75f to Color(0xFFFFFFFF).copy(alpha = 0.5f),
        1.0f to DefaultAppColors.greyScale.c80.copy(alpha = 1f)
    ), start = Offset(-500f, 100f), end = Offset(150f, -100f)
)

val borderStroke = BorderStroke(1.dp, brush = borderGradient)

// ---------- AI Insights ----------
@Composable
fun UnlockPremiumSection(
    title: String,
    subtitle: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CardContainer(
        modifier = modifier,
        containerColor = Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { onOpen() }.background(Color.White)
        ) {
            Icon(
                painterResource(Res.drawable.zigzag),
                modifier = Modifier.size(theme.dimension.largeIconSize * 4)
                    .padding(theme.dimension.veryLargeSpacing),
                contentDescription = "",
                tint = theme.colors.surface
            )
            Row(
                modifier = Modifier.padding(theme.dimension.largeSpacing),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
                ) {
                    Text(
                        title,
                        style = theme.typography.titleSmall,
                        color = theme.colors.surface
                    )

                    Text(
                        subtitle,
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.surface,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                    SmallButton(
                        text = "Show me",
                        contentColor = theme.colors.onPrimary,
                        border = true,
                        iconPosition = IconPosition.TRAILING,
//                        iconPainter = painterResource(Res.drawable.arrow_up_right),
                        backgroundColor = Color.Black,
                    )
                }
            }
        }

    }
}

// ---------- Tabs ----------
@Composable
fun HomeTabs(
    tabs: List<HomeTab>,
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecondaryScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selected),
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        edgePadding = 0.dp,
        divider = {},
        indicator = {}) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            Tab(selected = isSelected, onClick = { onSelect(tab) }, text = {
                Text(
                    tab.label,
                    style = theme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) theme.colors.onSurface else theme.colors.greyTextColor
                )
            })
        }
    }
}

// ---------- Holding row ----------
@Composable
fun HoldingRow(
    item: HoldingRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().clickable { onClick() }
        .padding(vertical = theme.dimension.largeSpacing),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {

        AssetLogoContainer(
            logoUrl = item.logoUrl,
            symbol = item.symbol
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
        ) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = theme.typography.bodyMediumStrong,
                color = theme.colors.onSurface
            )
            Text(
                text = item.symbol,
                style = theme.typography.bodyMediumMedium,
                color = theme.colors.onSurface
            )
        }


        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            Text(
                text = "%.2f".format(item.price),
                style = theme.typography.bodyMediumStrong,
                color =
                    theme.colors.onSurface
            )
            val changeText =
                (if (item.changePct >= 0) "▲" else "▼") + " " + "%.2f".format(item.changePct) + "%"
            Text(
                text = changeText,
                style = theme.typography.bodyMediumMedium,
                color = if (item.changePct >= 0) theme.colors.greenScale.c50 else theme.colors.error
            )
        }

    }
}

@Composable
fun RowScope.BottomNavigationItem(icon: ImageVector, selected: Boolean, onClick: () -> Unit = {}) {

    val color =
        animateColorAsState(if (selected) theme.colors.onSurface else theme.colors.greyScale.c50)

    val containerColor =
        animateColorAsState(if (selected) theme.colors.primary.c50 else Color.Transparent)

    Surface(
        modifier = Modifier.weight(1f),
        onClick = onClick,
        shape = CircleShape,
        color = containerColor.value,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(theme.dimension.largeSpacing)
                .padding(vertical = theme.dimension.mediumSpacing)
        ) {
            Icon(
                icon,
                contentDescription = "Bottom nav icon",
                tint = color.value,
                modifier = Modifier.size(theme.dimension.iconSize)
            )
        }
    }

}

// ---------- Bottom bar ----------
@Composable
fun HomeBottomBar(
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.HOME) }
    Surface(
        modifier = modifier.padding(
            horizontal = theme.dimension.veryLargeSpacing * 2,
            vertical = theme.dimension.veryLargeSpacing
        ).padding(bottom = theme.dimension.veryLargeSpacing),
        color = theme.colors.onPrimary,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.HOME, onClick = {
                    selectedTab = BottomNavItem.HOME
                }, icon = Icons.Rounded.Home
            )
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.STATS, onClick = {
                    selectedTab = BottomNavItem.STATS
                }, icon = Icons.Rounded.Equalizer
            )
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.SWAP, onClick = {
                    selectedTab = BottomNavItem.SWAP
                }, icon = Icons.Rounded.SwapHoriz
            )
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.ACCOUNT, onClick = {
                    selectedTab = BottomNavItem.ACCOUNT
                }, icon = Icons.Rounded.PersonOutline
            )

        }

    }
}

// ---------- Sample data ----------
//private fun sampleHoldings() = listOf(
//    HoldingRowUi("Apple", AssetClass.STOCK, +0.24, 269.07, 267.12, 271.31),
//    HoldingRowUi("Tesla", AssetClass.STOCK, +1.05, 449.53, 449.23, 551.12),
//    HoldingRowUi("Amazon", AssetClass.STOCK, -0.79, 244.77, 243.17, 246.03),
//    HoldingRowUi("Google", AssetClass.STOCK, +0.38, 286.82, 285.21, 287.35),
//    HoldingRowUi("Apple", AssetClass.REAL_ESTATE, +0.24, 269.07, 267.12, 271.31),
//    HoldingRowUi("Tesla", AssetClass.REAL_ESTATE, +1.05, 449.53, 449.23, 551.12),
//    HoldingRowUi("Amazon", AssetClass.STOCK, -0.79, 244.77, 243.17, 246.03),
//    HoldingRowUi("Google", AssetClass.REAL_ESTATE, +0.38, 286.82, 285.21, 287.35),
//    HoldingRowUi("Apple", AssetClass.STOCK, +0.24, 269.07, 267.12, 271.31),
//    HoldingRowUi("Tesla", AssetClass.STOCK, +1.05, 449.53, 449.23, 551.12),
//    HoldingRowUi("Amazon", AssetClass.REAL_ESTATE, -0.79, 244.77, 243.17, 246.03),
//    HoldingRowUi("Google", AssetClass.STOCK, +0.38, 286.82, 285.21, 287.35),
//    HoldingRowUi("Apple", AssetClass.STOCK, +0.24, 269.07, 267.12, 271.31),
//    HoldingRowUi("Tesla", AssetClass.CRYPTO, +1.05, 449.53, 449.23, 551.12),
//    HoldingRowUi("Amazon", AssetClass.STOCK, -0.79, 244.77, 243.17, 246.03),
//    HoldingRowUi("Google", AssetClass.CRYPTO, +0.38, 286.82, 285.21, 287.35),
//)

@Composable
fun ChipContainer(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(theme.dimension.verySmallRadius),
        color = theme.colors.primary.c20
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
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

enum class AssetClass {
    STOCK, REAL_ESTATE, CRYPTO,

}

val AssetClass.label: String
    get() = when (this) {
        AssetClass.STOCK -> "Stock"
        AssetClass.REAL_ESTATE -> "Real estate"
        AssetClass.CRYPTO -> "Crypto"
    }

fun String.format(value: Double): String {
    val rounded = round(value * 100) / 100
    return rounded.toString().let {
        val parts = it.split(".")
        when {
            parts.size == 1 -> it + ".00"
            parts[1].length == 1 -> it + "0"
            else -> it
        }
    }
}