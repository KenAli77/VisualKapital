package com.example.visualmoney.premium

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.domain.model.AssetQuote
import com.example.visualmoney.domain.model.StockNews
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.util.formatDecimal

@Composable
fun PremiumDashboardScreen(
    viewModel: PremiumViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAsset: (String) -> Unit
) {
    val theme = LocalAppTheme.current
    val state by viewModel.state.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Analysis", "Exposure", "News")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.colors.onSurface
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Premium Dashboard",
                style = theme.typography.titleMedium,
                color = theme.colors.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = theme.colors.primary.c50,
            divider = { }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                         Text(
                             text = title,
                             style = if (selectedTab == index) theme.typography.bodyMediumStrong else theme.typography.bodyMedium,
                             color = if (selectedTab == index) theme.colors.primary.c50 else theme.colors.greyScale.c50
                         )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(theme.dimension.mediumSpacing))

        if (state.isPremiumLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = theme.colors.primary.c50)
            }
        } else {
            when (selectedTab) {
                0 -> AnalysisTab(state.portfolioAnalysis, onNavigateToAsset)
                1 -> ExposureTab(state.sectorExposure, state.countryExposure)
                2 -> NewsTab(state.news)
            }
        }
    }
}

@Composable
fun AnalysisTab(analysis: PortfolioAnalysis?, onNavigateToAsset: (String) -> Unit) {
    val theme = LocalAppTheme.current
    
    if (analysis == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No portfolio data available", color = theme.colors.greyScale.c50)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = theme.dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        item {
            // Total Value Card
            CardContainer(
                modifier = Modifier.fillMaxWidth(),
                containerColor = theme.colors.surface
            ) {
                Column(modifier = Modifier.padding(theme.dimension.largeSpacing)) {
                   Text("Total Portfolio Value", style = theme.typography.bodySmall, color = theme.colors.greyScale.c50)
                   Spacer(modifier = Modifier.height(theme.dimension.closeSpacing))
                   Text(
                       "$${formatDecimal(analysis.totalValue)}",
                       style = theme.typography.titleMedium,
                       color = theme.colors.onSurface
                   )
                   Spacer(modifier = Modifier.height(theme.dimension.mediumSpacing))
                   
                   Row(verticalAlignment = Alignment.CenterVertically) {
                       Icon(
                           imageVector = if (analysis.totalGainLoss >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                           contentDescription = null,
                           tint = if (analysis.totalGainLoss >= 0) theme.colors.greenScale.c50 else theme.colors.error,
                           modifier = Modifier.size(16.dp)
                       )
                       Spacer(modifier = Modifier.width(4.dp))
                       Text(
                           "${if (analysis.totalGainLoss >= 0) "+" else ""}$${formatDecimal(analysis.totalGainLoss)} (${formatDecimal(analysis.totalGainLossPct)}%)",
                           style = theme.typography.bodyMediumStrong,
                           color = if (analysis.totalGainLoss >= 0) theme.colors.greenScale.c50 else theme.colors.error
                       )
                       Spacer(modifier = Modifier.width(8.dp))
                       Text("All time", style = theme.typography.bodySmall, color = theme.colors.greyScale.c50)
                   }
                }
            }
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
                // Top Gainer
                analysis.topGainer?.let { gainer ->
                    PerformanceCard(
                        modifier = Modifier.weight(1f),
                        title = "Top Gainer",
                        symbol = gainer.symbol,
                        changePct = gainer.changesPercentage,
                        isGain = true,
                        onClick = { onNavigateToAsset(gainer.symbol) }
                    )
                }
                
                // Top Loser
                analysis.topLoser?.let { loser ->
                    PerformanceCard(
                        modifier = Modifier.weight(1f),
                        title = "Top Loser",
                        symbol = loser.symbol,
                        changePct = loser.changesPercentage,
                        isGain = false,
                        onClick = { onNavigateToAsset(loser.symbol) }
                    )
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(
    modifier: Modifier = Modifier,
    title: String,
    symbol: String,
    changePct: Double,
    isGain: Boolean,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current
    CardContainer(
        modifier = modifier.clickable { onClick() },
        containerColor = theme.colors.surface
    ) {
        Column(modifier = Modifier.padding(theme.dimension.mediumSpacing)) {
            Text(title, style = theme.typography.bodySmall, color = theme.colors.greyScale.c50)
            Spacer(modifier = Modifier.height(theme.dimension.closeSpacing))
            Text(symbol, style = theme.typography.titleMedium, color = theme.colors.onSurface)
             Spacer(modifier = Modifier.height(theme.dimension.closeSpacing))
            Text(
                "${if (changePct > 0) "+" else ""}${formatDecimal(changePct)}%",
                style = theme.typography.bodyMediumStrong,
                color = if (isGain) theme.colors.greenScale.c50 else theme.colors.error
            )
        }
    }
}

@Composable
fun ExposureTab(sectorExposure: Map<String, Double>, countryExposure: Map<String, Double>) {
    val theme = LocalAppTheme.current
    var exposureType by remember { mutableStateOf(0) } // 0: Sector, 1: Country
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = theme.dimension.pagePadding),
         verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
         item {
             Row(modifier = Modifier.fillMaxWidth().background(theme.colors.surface, RoundedCornerShape(8.dp)).padding(4.dp)) {
                 TabButton(
                     modifier = Modifier.weight(1f),
                     text = "Sector",
                     isSelected = exposureType == 0,
                     onClick = { exposureType = 0 }
                 )
                 TabButton(
                      modifier = Modifier.weight(1f),
                      text = "Country",
                      isSelected = exposureType == 1,
                      onClick = { exposureType = 1 }
                 )
             }
         }
         
         val data = if (exposureType == 0) sectorExposure else countryExposure
         val sortedData = data.toList().sortedByDescending { it.second }
         
         items(sortedData) { (name, percentage) ->
             ExposureItem(name, percentage)
         }
    }
}

@Composable
fun TabButton(modifier: Modifier, text: String, isSelected: Boolean, onClick: () -> Unit) {
    val theme = LocalAppTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) theme.colors.primary.c50 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = theme.typography.bodyMediumStrong,
            color = if (isSelected) theme.colors.surface else theme.colors.greyScale.c50
        )
    }
}

@Composable
fun ExposureItem(name: String, percentage: Double) {
    val theme = LocalAppTheme.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = theme.typography.bodyMedium, color = theme.colors.onSurface)
            Text("${formatDecimal(percentage)}%", style = theme.typography.bodyMediumStrong, color = theme.colors.onSurface)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = theme.colors.primary.c50,
            trackColor = theme.colors.surface,
        )
    }
}

@Composable
fun NewsTab(newsList: List<StockNews>) {
    val theme = LocalAppTheme.current
    
    if (newsList.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No news available for your portfolio", color = theme.colors.greyScale.c50)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = theme.dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        items(newsList) { news ->
            CardContainer(
                modifier = Modifier.fillMaxWidth(),
                containerColor = theme.colors.surface
            ) {
                Row(modifier = Modifier.padding(theme.dimension.mediumSpacing)) {
                    AsyncImage(
                        model = news.image,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(theme.dimension.mediumSpacing))
                    Column {
                        Text(
                            text = news.title,
                            style = theme.typography.bodyMediumStrong,
                            color = theme.colors.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${news.site} • ${news.publishedDate.take(10)}",
                            style = theme.typography.bodySmall,
                            color = theme.colors.greyScale.c50
                        )
                    }
                }
            }
        }
    }
}
