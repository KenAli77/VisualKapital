package com.example.visualmoney.home

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.greyTextColor
import kotlin.math.round


val theme @Composable get() = LocalAppTheme.current

// ---------- Models ----------
data class HoldingRowUi(
    val name: String,
    val assetClass: AssetClass,
    val changePct: Double,
    val price: Double,
    val dayLow: Double,
    val dayHigh: Double,
)

enum class HomeTab(val label: String) {
    Favourites("Favourites"),
    HotDeals("Hot Deals"),
    News("News"),
    Gainers("Gainers"),
    Losers("Losers"),
    H24("24h"),
}

// ---------- Screen ----------
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "James",
    balanceUsd: Double = 5738.25,
    profitUsd: Double = 295.83,
    mlPct: Double = 300.00,
    tabs: List<HomeTab> = HomeTab.entries,
    holdings: List<HoldingRowUi> = sampleHoldings(),
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Favourites) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = theme.colors.surface,
        bottomBar = { HomeBottomBar() },
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
            item { HomeTopHeader(userName = userName) }

            item {
                BalanceCard(
                    balanceUsd = balanceUsd,
                    profitUsd = profitUsd,
                    mlPct = mlPct,
                    onOpen = {},
                    onCurrencyClick = {},
                )
            }

            item {
                QuickActionsRow(
                    onAddMoney = {},
                    onTrade = {},
                    onWithdraw = {},
                )
            }

            item {
                AiInsightsCard(
                    title = "AI Insights",
                    subtitle = "Analyse Your Spending Activity",
                    onOpen = {}
                )
            }

            item {
                HomeTabs(
                    tabs = tabs,
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }

            items(holdings) { item ->
                HoldingRow(item = item, onClick = {})
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
                    text = "Available Balance",
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
                        style = theme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = theme.colors.onSurface
                    )
                    Text(
                        text = "." + text.format(balanceUsd).substringAfterLast("."),
                        style = theme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                        color = theme.colors.greyTextColor
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
    onAddMoney: () -> Unit,
    onTrade: () -> Unit,
    onWithdraw: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            title = "Add Money",
            icon = Icons.Rounded.AddCircleOutline,
            onClick = onAddMoney,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Tread Move",
            icon = Icons.Rounded.SyncAlt,
            onClick = onTrade,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Withdraw",
            icon = Icons.Rounded.AccountBalanceWallet,
            onClick = onWithdraw,
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
        modifier = modifier,
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, color = theme.colors.greyScale.c30),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(theme.dimension.veryLargeSpacing),
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
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.padding(theme.dimension.largeSpacing)
                        .size(theme.dimension.iconSize)

                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = theme.typography.titleSmallMedium)
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(theme.dimension.defaultRadius))
                    .background(theme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Business,
                    contentDescription = null,
                    modifier = Modifier.size(theme.dimension.iconSize)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = theme.typography.titleSmall,
                    )
                    Spacer(Modifier.width(8.dp))

                    AssetCategoryChip(assetClass = item.assetClass)
                }

                Spacer(Modifier.height(6.dp))

                // Low/High line (simple)
                Text(
                    text = "Min ${"%.2f".format(item.dayLow)}   Max ${"%.2f".format(item.dayHigh)}",
                    style = theme.typography.bodySmall,
                    color = theme.colors.onSurface
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f".format(item.price),
                    style = theme.typography.titleSmall,
                )
                val changeText =
                    (if (item.changePct >= 0) "+" else "") + "%.2f".format(item.changePct) + "%"
                Text(
                    text = changeText,
                    style = theme.typography.bodySmall,
                    color = if (item.changePct >= 0) theme.colors.greenScale.c50 else theme.colors.error
                )
            }
        }
    }
}

// ---------- Bottom bar ----------
@Composable
fun HomeBottomBar(
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.Equalizer, contentDescription = "Stats") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.SwapHoriz, contentDescription = "Trade") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Activity") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.PersonOutline, contentDescription = "Profile") }
        )
    }
}

// ---------- Sample data ----------
private fun sampleHoldings() = listOf(
    HoldingRowUi("Apple", AssetClass.STOCK, +0.24, 269.07, 267.12, 271.31),
    HoldingRowUi("Tesla", AssetClass.STOCK, +1.05, 449.53, 449.23, 551.12),
    HoldingRowUi("Amazon", AssetClass.STOCK, -0.79, 244.77, 243.17, 246.03),
    HoldingRowUi("Google", AssetClass.STOCK, +0.38, 286.82, 285.21, 287.35),
)

@Composable
fun AssetCategoryChip(modifier: Modifier = Modifier, assetClass: AssetClass) {
    Surface(modifier = modifier,shape = RoundedCornerShape(theme.dimension.verySmallRadius), color = theme.colors.greyScale.c10) {
        Box(contentAlignment = Alignment.Center){
            Text(assetClass.label, style = theme.typography.bodySmallMedium, color = theme.colors.greyScale.c50,modifier = Modifier.padding(horizontal = theme.dimension.closeSpacing, vertical = theme.dimension.veryCloseSpacing))
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