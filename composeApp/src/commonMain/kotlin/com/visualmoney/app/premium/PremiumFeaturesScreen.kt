package com.visualmoney.app.premium

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visualmoney.app.DarkBackgroundGradient
import com.visualmoney.app.LocalAppTheme
import com.visualmoney.app.core.TopNavigationBar
import com.revenuecat.purchases.kmp.Purchases
import com.visualmoney.app.home.CardContainer

@Composable
fun PremiumFeaturesScreen(
    viewModel: PremiumViewModel,
    purchases: Purchases,
    onNavigateBack: () -> Unit,
    onNavigateToAsset: (String) -> Unit,
    onPurchaseComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val state by viewModel.state.collectAsState()

    // Handle purchase success
    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            onPurchaseComplete()
            viewModel.resetPurchaseSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (state.isPremium) {
            PremiumDashboardScreen(
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onNavigateToAsset = onNavigateToAsset
            )
        } else {
            Paywall(
                viewModel = viewModel,
                state = state,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

// PremiumDashboard is now in its own file


@Composable
fun Paywall(
    viewModel: PremiumViewModel,
    state: PremiumState,
    onNavigateBack: () -> Unit
) {
    val theme = LocalAppTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(theme.dimension.pagePadding),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)
    ) {
        // Header with back button
        TopNavigationBar(
            title = "Premium",
            subtitle = "Gain insights and tools to maximize your investment potential.",
            onBack = onNavigateBack
        )
        FeatureHighlight(
            Icons.Rounded.Analytics,
            "Portfolio Analysis",
            "Deep dive into your performance"
        )
        FeatureHighlight(
            Icons.Rounded.Language,
            "Risk & Geographic Exposure",
            "Understand your risk distribution"
        )
        FeatureHighlight(
            Icons.Rounded.Newspaper,
            "News on Your Assets",
            "Real-time updates for your holdings"
        )

        Spacer(modifier = Modifier.height(theme.dimension.veryLargeSpacing * 2))

        // Subscription Packages
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = theme.colors.primary.c50)
            }
        } else {
            state.packages.forEach { packageInfo ->
                SubscriptionCard(
                    packageInfo = packageInfo,
                    onClick = { viewModel.triggerPurchase(packageInfo) },
                    isEnabled = !state.isLoading
                )
                Spacer(modifier = Modifier.height(theme.dimension.mediumSpacing))
            }
        }

        // Error Message
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(theme.dimension.mediumSpacing))
            Text(
                text = error,
                style = theme.typography.bodySmall,
                color = theme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(theme.dimension.veryLargeSpacing))
        Text(
            text = "Restore Purchases",
            style = theme.typography.bodySmall,
            color = theme.colors.greyScale.c50,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { viewModel.restorePurchases() }
        )
    }
}

@Composable
fun FeatureHighlight(icon: ImageVector, title: String, subtitle: String) {
    val theme = LocalAppTheme.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
        Icon(
            icon,
            contentDescription = null,
            tint = theme.colors.primary.c50,
            modifier = Modifier.size(theme.dimension.largeIconSize)
        )
        Column {
            Text(
                text = title,
                style = theme.typography.titleSmall,
                color = theme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = theme.typography.bodyMedium,
                color = theme.colors.greyScale.c50
            )
        }
    }
}

@Composable
fun SubscriptionCard(
    packageInfo: PackageInfo,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    val theme = LocalAppTheme.current

    val isBestValue = packageInfo.type == PackageType.LIFETIME // Just an example logic

    CardContainer(
        modifier = Modifier.fillMaxWidth().clickable(enabled = isEnabled, onClick = onClick),
        containerColor = theme.colors.surface, // Made uniform color
    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isBestValue) {
                    Text(
                        text = "BEST VALUE",
                        style = theme.typography.bodySmall,
                        color = theme.colors.primary.c50,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = when (packageInfo.type) {
                        PackageType.MONTHLY -> "Monthly"
                        PackageType.ANNUAL -> "Yearly"
                        PackageType.LIFETIME -> "Lifetime"
                        else -> packageInfo.title
                    },
                    style = theme.typography.titleSmall,
                    color = theme.colors.onSurface
                )
                Text(
                    text = packageInfo.description,
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = packageInfo.priceString,
                    style = theme.typography.titleMedium,
                    color = theme.colors.onSurface
                )
                Text(
                    text = when (packageInfo.type) {
                        PackageType.MONTHLY -> "/mo"
                        PackageType.ANNUAL -> "/yr"
                        else -> ""
                    },
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyScale.c50
                )
            }
        }
    }
}
