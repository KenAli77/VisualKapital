package com.example.visualmoney.home

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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AreaChart
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.greyTextColor
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
    Favourites("Top movers"),
    HotDeals("Upcoming events"),
    News("Losers"),
    Gainers("Gainers"),
    Losers("Losers"),
    H24("24h"),
}

enum class BottomNavItem {
    HOME,
    STATS,
    SWAP,
    ACCOUNT
}

// ---------- Screen ----------
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    userName: String = "James",
    balanceUsd: Double = 5738.25,
    profitUsd: Double = 295.83,
    mlPct: Double = 300.00,
    tabs: List<HomeTab> = HomeTab.entries,
    onGoToCalendar: () -> Unit = {},
    onGoToBalance: () -> Unit = {},
    onGoToNews: () -> Unit = {},
    onGoToAssetDetails: (String) -> Unit = {}
) = with(viewModel) {
    var selectedTab by remember { mutableStateOf(HomeTab.Favourites) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = theme.colors.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = theme.dimension.pagePadding)
                .padding(top = theme.dimension.pagePadding)
            ,
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
//            contentPadding = PaddingValues(
//                top = theme.dimension.pagePadding,
//                bottom = theme.dimension.pagePadding
//            )
        ) {
            HomeTopHeader(userName = userName)
            BalanceCard(
                balanceUsd = balanceUsd,
                profitUsd = profitUsd,
                mlPct = mlPct,
                onOpen = {},
                onCurrencyClick = {},
            )
            QuickActionsRow(
                onAddAsset = {

                },
                onGoToCalendar = {
                    onGoToCalendar()
                },
                onGoToNews = {
                    onGoToNews()
                },
            )
            AiInsightsCard(
                title = "Unlock Insights",
                subtitle = "Analyse Your Portfolio",
                onOpen = {}
            )
            HomeTabs(
                tabs = tabs,
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
            LazyColumn(
                modifier = Modifier.border(
                    width = 1.dp,
                    color = theme.colors.greyScale.c30,
                    shape = RoundedCornerShape(theme.dimension.defaultRadius)
                )
            ) {
                items(state.holdings) { item ->
                    HoldingRow(
                        modifier = Modifier.padding(horizontal = theme.dimension.mediumSpacing),
                        item = item,
                        onClick = {
                            onGoToAssetDetails(item.symbol)
                        })
                    HorizontalDivider(thickness = 1.dp, color = theme.colors.greyScale.c30)

                }

            }
        }
    }
}

// ---------- Header ----------
@Composable
fun HomeTopHeader(
    userName: String,
    modifier: Modifier = Modifier,
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
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(theme.colors.surface)
                    .border(1.dp, theme.colors.greyScale.c40, CircleShape)
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
                onClick = { /* scan */ },
                Icons.Rounded.Notifications,
                contentDescription = "Scan",
            )
            IconWithContainer(
                onClick = {},
                Icons.Rounded.Search,
                contentDescription = "Search",
            )
        }


    }
}

@Composable
fun IconWithContainer(
    onClick: () -> Unit = {},
    icon: ImageVector,
    contentDescription: String = "",
    containerColor: Color = theme.colors.container,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(CircleShape)
            .background(containerColor)
            .clickable {
                onClick()
            }
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(theme.dimension.mediumSpacing)
                .size(theme.dimension.iconSize)
        )

    }

}

@Composable
fun IconWithContainerSmall(
    onClick: () -> Unit = {},
    icon: ImageVector,
    contentDescription: String = "",
    containerColor: Color = theme.colors.surface,
    contentColor: Color = theme.colors.onSurface,
    shape: Shape = RoundedCornerShape(theme.dimension.smallRadius),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(shape)
            .background(containerColor)
            .clickable {
                onClick()
            }
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(6.dp)
                .size(theme.dimension.smallIconSize),
            tint = contentColor
        )
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
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onOpen,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = theme.colors.container
        )
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
            ) {
                IconWithContainerSmall(
                    onClick = {},
                    icon = Icons.AutoMirrored.Rounded.ShowChart,
                    contentDescription = "Balance",
                )
                Text(
                    text = "Portfolio value",
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyTextColor
                )
                Spacer(Modifier.weight(1f))
                IconWithContainerSmall(
                    {},
                    icon = Icons.Rounded.NorthEast,
                    containerColor = theme.colors.primary.c50,
                    shape = CircleShape
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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

// ---------- Quick actions ----------
@Composable
fun QuickActionsRow(
    onAddAsset: () -> Unit,
    onGoToCalendar: () -> Unit,
    onGoToNews: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        QuickActionButton(
            title = "Add asset",
            icon = Icons.Rounded.AddCircleOutline,
            onClick = onAddAsset,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Calendar",
            icon = Icons.Rounded.CalendarToday,
            onClick = onGoToCalendar,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "News",
            icon = Icons.Rounded.Newspaper,
            onClick = onGoToNews,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.wrapContentHeight(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, color = theme.colors.greyScale.c30),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(theme.dimension.veryLargeSpacing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    theme.dimension.mediumSpacing,
                    alignment = Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(theme.dimension.iconSize)
                )
                Text(
                    text = title,
                    style = theme.typography.bodySmallMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

val primaryGradient = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFFFFBC42),
        0.5f to Color(0xFFFFC653),
        1.0f to Color(0xFFFFA904)
    ),
    start = Offset(0f, 0f),
    end = Offset.Infinite
)

// ---------- AI Insights ----------
@Composable
fun AiInsightsCard(
    title: String,
    subtitle: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Base component: use Card + gradient-like feel via surface background.
    // Swap to Brush.horizontalGradient(...) if you want a real gradient.
    Surface(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(theme.dimension.defaultRadius))
            .background(brush = primaryGradient),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        onClick = onOpen,
        color = Color.Transparent
//        colors = CardDefaults.cardColors(containerColor = Color.Transparent),

    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(theme.dimension.defaultRadius))
                    .background(theme.colors.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AreaChart,
                    contentDescription = null,
                    modifier = Modifier.padding(theme.dimension.largeSpacing)
                        .size(theme.dimension.iconSize)

                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = theme.typography.titleSmall)
                Text(
                    subtitle,
                    style = theme.typography.bodySmall,
                    color = theme.colors.onSurface
                )
            }

            Icon(
                Icons.Rounded.NorthEast,
                contentDescription = "Open",
                modifier = Modifier.size(theme.dimension.iconSize)
            )
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
        indicator = {}
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            Tab(
                selected = isSelected,
                onClick = { onSelect(tab) },
                text = {
                    Text(
                        tab.label,
                        style = theme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) theme.colors.onSurface else theme.colors.greyTextColor
                    )
                }
            )
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.colors.onPrimary)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.logoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
                ) {
                    Text(
                        modifier = Modifier.weight(1f, fill = true),
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = theme.typography.bodyMediumMedium,
                    )
                    AssetCategoryChip(
                        modifier = Modifier.wrapContentWidth(),
                        assetClass = item.assetClass
                    )
                }
                // Low/High line (simple)
                Text(
                    text = "Min ${"%.2f".format(item.dayLow)}   Max ${"%.2f".format(item.dayHigh)}",
                    style = theme.typography.bodySmall,
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
                )
                val changeText =
                    (if (item.changePct >= 0) "+" else "") + "%.2f".format(item.changePct) + "%"
                Text(
                    text = changeText,
                    style = theme.typography.bodyMediumMedium,
                    color = if (item.changePct >= 0) theme.colors.greenScale.c50 else theme.colors.error
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomNavigationItem(icon: ImageVector, selected: Boolean, onClick: () -> Unit = {}) {

    var color =
        animateColorAsState(if (selected) theme.colors.onSurface else theme.colors.greyScale.c50)

    var containerColor =
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
                selected = selectedTab == BottomNavItem.HOME,
                onClick = {
                    selectedTab = BottomNavItem.HOME
                },
                icon = Icons.Rounded.Home
            )
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.STATS,
                onClick = {
                    selectedTab = BottomNavItem.STATS
                },
                icon = Icons.Rounded.Equalizer
            )
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.SWAP,
                onClick = {
                    selectedTab = BottomNavItem.SWAP
                },
                icon = Icons.Rounded.SwapHoriz
            )
            BottomNavigationItem(
                selected = selectedTab == BottomNavItem.ACCOUNT,
                onClick = {
                    selectedTab = BottomNavItem.ACCOUNT
                },
                icon = Icons.Rounded.PersonOutline
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
fun AssetCategoryChip(modifier: Modifier = Modifier, assetClass: AssetClass) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        color = theme.colors.greyScale.c10
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                assetClass.label,
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
    STOCK,
    REAL_ESTATE,
    CRYPTO,

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