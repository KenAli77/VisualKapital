package com.visualmoney.app.portfolioOverview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.visualmoney.app.core.TopNavigationBar
import com.visualmoney.app.greyTextColor
import com.visualmoney.app.home.BalanceCard
import com.visualmoney.app.home.CardContainer
import com.visualmoney.app.home.HomeViewModel
import com.visualmoney.app.home.PortfolioDistributionItem
import com.visualmoney.app.home.categoryPerformanceString
import com.visualmoney.app.home.color
import com.visualmoney.app.home.format
import com.visualmoney.app.home.theme
import com.visualmoney.app.home.trackColor
import kotlin.math.roundToInt

@Composable
fun PortfolioOverviewScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onBackPressed: () -> Unit
) = with(viewModel) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)
        ) {
            TopNavigationBar(title = "Portfolio", onBack = onBackPressed)
            BalanceCard(viewModel.state.metrics, onOpen = {}, onCurrencyClick = {}, forOverview = true)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
            ) {
                Text(
                    "Portfolio distribution",
                    style = theme.typography.bodyMediumStrong,
                    color = theme.colors.greyTextColor
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                    items(viewModel.state.distribution) {
                        AssetDistributionItem(item = it)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetDistributionItem(modifier: Modifier = Modifier, item: PortfolioDistributionItem) {
    CardContainer(modifier = modifier.fillMaxWidth(), containerColor = theme.colors.surface) {
        Row(modifier = Modifier.fillMaxWidth().padding(theme.dimension.largeSpacing), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
            AssetDistributionBadge(
                percentage = item.percentage,
                color = item.color,
                trackColor = item.trackColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        item.label,
                        style = theme.typography.bodyMediumMedium,
                        color = theme.colors.onSurface
                    )
                    Text(
                        text = "${item.productCount} products",
                        style = theme.typography.bodyMedium,
                        color = theme.colors.greyTextColor
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                    horizontalAlignment = Alignment.End
                ) {
                    val text = "$" + "%.2f".format(item.totalValue)
                    Text(
                        text,
                        style = theme.typography.bodyMediumStrong,
                        color = theme.colors.onSurface
                    )

                    Text(
                        text = item.categoryPerformanceString("$"),
                        style = theme.typography.bodySmallStrong,
                        color = theme.colors.greyTextColor
                    )

                }
            }
        }
    }
}

@Composable
fun AssetDistributionBadge(modifier: Modifier = Modifier, percentage: Double, color: Color,trackColor: Color) {
    val progress = (percentage / 100).toFloat()

    Box(modifier = modifier.clip(CircleShape), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            strokeWidth = 3.dp,
            progress = { progress }
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(trackColor)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${percentage.roundToInt()}%",
                modifier = Modifier,
                style = theme.typography.bodySmall,
                color = theme.colors.onSurface,
            )
        }
    }

}
