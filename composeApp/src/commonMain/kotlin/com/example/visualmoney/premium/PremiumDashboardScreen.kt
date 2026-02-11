package com.example.visualmoney.premium

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.core.TopNavigationBar
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
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        TopNavigationBar(title = "Portfolio Analysis", onBack = onNavigateBack )

        if (state.isPremiumLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = theme.colors.primary.c50)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
            ) {
                // 1. The Pulse (Risk Score & Key Metrics)
                item {
                    PulseSection(state.metrics)
                }
                
                // 2. Portfolio Value & Performance
                item {
                    PortfolioValueSection(state.metrics)
                }
                
                // 3. Risk Metrics (Volatility, Beta, VaR)
                item {
                    RiskMetricsSection(state.metrics)
                }
                
                // 4. Allocation (Sector, Geo, Asset Class)
                item {
                    AllocationSection(
                        sectorExposure = state.sectorExposure,
                        countryExposure = state.countryExposure,
                        assetClassExposure = state.assetClassExposure
                    )
                }
                
                // 5. Concentration Alerts
                if (state.concentrationAlerts.isNotEmpty()) {
                    item {
                        ConcentrationSection(state.concentrationAlerts, onNavigateToAsset)
                    }
                }
                
                // 6. Insights & High Risk Assets
                item {
                    InsightsSection(state.metrics, state.highRiskAssets, onNavigateToAsset)
                }
            }
        }
    }
}

// ---------------- Sections ----------------

@Composable
fun PulseSection(metrics: PortfolioMetrics) {
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier.padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.mediumSpacing)
    ) {
        Text(
            "The Pulse",
            style = theme.typography.titleSmall,
            color = theme.colors.greyScale.c50,
            modifier = Modifier.padding(bottom = theme.dimension.closeSpacing)
        )
        
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = theme.colors.surface
        ) {
            Row(
                modifier = Modifier.padding(theme.dimension.largeSpacing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Risk Score Gauge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RiskScoreGauge(
                            score = (metrics.riskScore / 10).coerceIn(0, 10),
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${metrics.riskScore}",
                                style = theme.typography.titleLarge,
                                color = theme.colors.onSurface
                            )
                            Text(
                                "Risk Score",
                                style = theme.typography.bodySmall,
                                color = theme.colors.greyScale.c50
                            )
                        }
                    }
                }
                
                // Key Metrics: Sharpe & Alpha
                Column(
                    modifier = Modifier.padding(start = theme.dimension.largeSpacing),
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
                ) {
                    MetricRow(
                        label = "Sharpe Ratio",
                        value = formatDecimal(metrics.sharpeRatio),
                        isGood = metrics.sharpeRatio > 1.0,
                        description = "Risk-adjusted return"
                    )
                    
                    // Alpha calculated roughly as Return - (Beta * MarketReturn(10%))
                    // Just displaying Alpha if > 0 otherwise Beta
                    MetricRow(
                        label = "Beta",
                        value = formatDecimal(metrics.beta),
                        isGood = metrics.beta < 1.2, // Subjective
                        description = "Market sensitivity"
                    )
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, isGood: Boolean, description: String) {
    val theme = LocalAppTheme.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = label, style = theme.typography.titleMedium, color = theme.colors.onSurface)
            },
            text = {
                Text(text = description, style = theme.typography.bodyMedium, color = theme.colors.onSurface)
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", color = theme.colors.primary.c50)
                }
            },
            containerColor = theme.colors.surface
        )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                label,
                style = theme.typography.bodySmallMedium,
                color = theme.colors.greyScale.c50
            )
            Text(
                value,
                style = theme.typography.titleMedium,
                color = if (isGood) theme.colors.greenScale.c50 else theme.colors.onSurface
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            Icons.Rounded.Info,
            contentDescription = "Info",
            tint = theme.colors.greyScale.c80,
            modifier = Modifier
                .size(16.dp)
                .clickable { showDialog = true }
        )
    }
}

@Composable
fun PortfolioValueSection(metrics: PortfolioMetrics) {
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier.padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.closeSpacing)
    ) {
        CardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = theme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(theme.dimension.largeSpacing)
            ) {
                Text(
                    "Total Portfolio Value",
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
                Text(
                    "$${formatDecimal(metrics.totalValue)}",
                    style = theme.typography.titleLarge,
                    color = theme.colors.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val isProfit = metrics.totalReturn >= 0
                    Icon(
                        if (isProfit) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = if (isProfit) theme.colors.greenScale.c50 else theme.colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${if (isProfit) "+" else ""}$${formatDecimal(metrics.totalReturn)} (${formatDecimal(metrics.totalReturnPct)}%)",
                        style = theme.typography.bodyMediumStrong,
                        color = if (isProfit) theme.colors.greenScale.c50 else theme.colors.error
                    )
                    Text(
                        " All time",
                        style = theme.typography.bodySmall,
                        color = theme.colors.greyScale.c50
                    )
                }
                
                // Drawdown visualization (simplified)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Max Drawdown", style = theme.typography.bodySmall, color = theme.colors.greyScale.c50)
                    Text("N/A", style = theme.typography.bodySmallStrong, color = theme.colors.onSurface) // Requires historical data
                }
                LinearProgressIndicator(
                    progress = { 0.1f }, // Placeholder
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = theme.colors.error.copy(alpha = 0.7f),
                    trackColor = theme.colors.greyScale.c90
                )
            }
        }
    }
}

@Composable
fun RiskMetricsSection(metrics: PortfolioMetrics) {
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier.padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.mediumSpacing)
    ) {
        Text(
            "Risk Metrics",
            style = theme.typography.titleSmall,
            color = theme.colors.greyScale.c50,
            modifier = Modifier.padding(bottom = theme.dimension.closeSpacing)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            // Volatility Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Volatility",
                value = "${formatDecimal(metrics.volatility)}%",
                subtitle = "Std Dev",
                color = if (metrics.volatility > 2.0) theme.colors.error else theme.colors.primary.c50
            )
            
            // VaR Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Value at Risk",
                value = "$${formatDecimal(metrics.valueAtRisk)}",
                subtitle = "95% Conf.",
                color = theme.colors.error
            )
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, title: String, value: String, subtitle: String, color: Color) {
    val theme = LocalAppTheme.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = title, style = theme.typography.titleMedium, color = theme.colors.onSurface)
            },
            text = {
                Text(text = "Explanation for $title: $subtitle", style = theme.typography.bodyMedium, color = theme.colors.onSurface)
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", color = theme.colors.primary.c50)
                }
            },
            containerColor = theme.colors.surface
        )
    }

    CardContainer(
        modifier = modifier.clickable { showDialog = true }, // Make whole card clickable or just adding icon
        containerColor = theme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Text(title, style = theme.typography.bodySmall, color = theme.colors.greyScale.c50)
                 Icon(
                    Icons.Rounded.Info,
                    contentDescription = "Info",
                    tint = theme.colors.greyScale.c80,
                    modifier = Modifier.size(14.dp)
                 )
            }
           
            Text(value, style = theme.typography.titleMedium, color = color)
            Text(subtitle, style = theme.typography.bodySmall, color = theme.colors.greyScale.c60)
        }
    }
}

@Composable
fun AllocationSection(
    sectorExposure: List<ExposureItem>,
    countryExposure: List<ExposureItem>,
    assetClassExposure: List<ExposureItem>
) {
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier.padding(vertical = theme.dimension.mediumSpacing)
    ) {
        Text(
            "Allocation",
            style = theme.typography.titleSmall,
            color = theme.colors.greyScale.c50,
            modifier = Modifier.padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.closeSpacing)
        )
        
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = theme.dimension.pagePadding),
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            item {
                AllocationCard(title = "By Sector", data = sectorExposure)
            }
            item {
                AllocationCard(title = "By Geography", data = countryExposure)
            }
            item {
                AllocationCard(title = "By Asset Class", data = assetClassExposure)
            }
        }
    }
}

@Composable
fun AllocationCard(title: String, data: List<ExposureItem>) {
    val theme = LocalAppTheme.current
    CardContainer(
        modifier = Modifier.width(300.dp),
        containerColor = theme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing)
        ) {
            Text(title, style = theme.typography.bodyMediumStrong, color = theme.colors.onSurface)
            Spacer(modifier = Modifier.height(theme.dimension.mediumSpacing))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(data, Modifier.fillMaxSize())
                }
                
                Spacer(modifier = Modifier.width(theme.dimension.largeSpacing))
                
                // Legend (Top 3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.take(3).forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(item.color.toInt()), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                item.name,
                                style = theme.typography.bodySmall,
                                color = theme.colors.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "${item.percentage.toInt()}%",
                                style = theme.typography.bodySmallStrong,
                                color = theme.colors.greyScale.c50
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(data: List<ExposureItem>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val total = data.sumOf { it.percentage }
        var startAngle = -90f
        val strokeWidth = 30f
        
        data.forEach { item ->
            val sweepAngle = (item.percentage / total * 360).toFloat()
            drawArc(
                color = Color(item.color.toInt()),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}


@Composable
fun ConcentrationSection(alerts: List<ConcentrationAlert>, onNavigateToAsset: (String) -> Unit) {
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier.padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.mediumSpacing)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Warning, contentDescription = null, tint = theme.colors.error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Concentration Alerts",
                style = theme.typography.titleSmall,
                color = theme.colors.error
            )
        }
        
        Spacer(modifier = Modifier.height(theme.dimension.closeSpacing))
        
        alerts.forEach { alert ->
            CardContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onNavigateToAsset(alert.symbol) },
                containerColor = theme.colors.error.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(theme.dimension.mediumSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${alert.symbol}: ${formatDecimal(alert.percentage)}% of portfolio",
                        style = theme.typography.bodyMediumStrong,
                        color = theme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "> ${alert.threshold.toInt()}% limit",
                        style = theme.typography.bodySmall,
                        color = theme.colors.error
                    )
                }
            }
        }
    }
}

// Re-using simplified RiskScoreGauge
@Composable
fun RiskScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val activeColor = if(score >= 7) theme.colors.greenScale.c50 else if(score >= 4) Color(0xFFFFA500) else theme.colors.error
    val trackColor = theme.colors.greyScale.c80
    
    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        val startAngle = 135f
        val sweepAngle = 270f
        
        // Track
        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Active
        drawArc(
            color = activeColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle * (score / 10f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun InsightsSection(metrics: PortfolioMetrics, highRiskAssets: List<HighRiskAsset>, onNavigateToAsset: (String) -> Unit) {
    val theme = LocalAppTheme.current
    
    Column(
        modifier = Modifier.padding(horizontal = theme.dimension.pagePadding, vertical = theme.dimension.mediumSpacing)
    ) {
        Text(
            "Insights & Actions",
            style = theme.typography.titleSmall,
            color = theme.colors.greyScale.c50,
            modifier = Modifier.padding(bottom = theme.dimension.closeSpacing)
        )
        
        // Rebalancing Suggestion (Mock)
        CardContainer(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            containerColor = theme.colors.surface
        ) {
            Column(modifier = Modifier.padding(theme.dimension.largeSpacing)) {
                Text("Rebalancing Suggestion", style = theme.typography.bodyMediumStrong, color = theme.colors.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    metrics.rebalancingSuggestion,
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
            }
        }

         // Dividend Insights
        CardContainer(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            containerColor = theme.colors.surface
        ) {
            Column(modifier = Modifier.padding(theme.dimension.largeSpacing)) {
                Text("Dividend Outlook", style = theme.typography.bodyMediumStrong, color = theme.colors.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                     "Projected annual income based on current yields.",
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$${formatDecimal(metrics.estimatedAnnualIncome)}",
                        style = theme.typography.titleLarge,
                        color = theme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = theme.colors.greenScale.c10,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "${formatDecimal(metrics.currentYield)}% Yield",
                            style = theme.typography.bodySmallStrong,
                            color = theme.colors.greenScale.c50,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        
        // High Risk Assets List
        if (highRiskAssets.isNotEmpty()) {
            Text(
                "High Risk Assets",
                style = theme.typography.bodyMediumStrong,
                color = theme.colors.greyScale.c50,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                highRiskAssets.forEach { asset ->
                    HighRiskAssetItem(asset, onNavigateToAsset)
                }
            }
        }
    }
}

@Composable
fun HighRiskAssetItem(
    asset: HighRiskAsset,
    onClick: (String) -> Unit
) {
    val theme = LocalAppTheme.current
    
    CardContainer(
        modifier = Modifier.fillMaxWidth().clickable { onClick(asset.symbol) },
        containerColor = theme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.colors.greyScale.c80),
                contentAlignment = Alignment.Center
            ) {
                if (asset.logoUrl != null) {
                    AsyncImage(
                        model = asset.logoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        asset.symbol.take(3),
                        style = theme.typography.bodySmallMedium,
                        color = theme.colors.onSurface
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
            ) {
                Text(
                    asset.name,
                    style = theme.typography.bodyMediumStrong,
                    color = theme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    asset.sector,
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
            }
            
            Text(
                "${formatDecimal(asset.percentage)}%",
                style = theme.typography.bodyMediumStrong,
                color = theme.colors.error
            )
        }
    }
}
