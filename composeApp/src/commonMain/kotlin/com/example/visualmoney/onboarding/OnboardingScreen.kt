package com.example.visualmoney.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.visualmoney.DarkBackgroundGradient
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.LargeButton
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.borderGradient
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.theme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(theme.dimension.pagePadding)
                .padding(vertical = theme.dimension.veryLargeSpacing * 2),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Page Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = theme.dimension.closeSpacing)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) theme.colors.primary.c50
                                else theme.colors.greyScale.c70
                            )
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),

            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        userName = state.userName,
                        onUserNameChange = viewModel::updateUserName
                    )

                    1 -> FeaturePage(
                        icon = Icons.Rounded.Wallet,
                        title = "Track Your Portfolio",
                        description = "Monitor all your investments in one place. Stocks, crypto, real estate, and more."
                    )

                    2 -> FeaturePage(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "Smart Reminders",
                        description = "Never miss important dates. Set reminders for dividends, earnings, and investment goals."
                    )

                    3 -> FeaturePage(
                        icon = Icons.Rounded.ShowChart,
                        title = "Real-time Market Data",
                        description = "Stay updated with live prices, charts, and market insights for all your assets."
                    )
                }
            }

            // Navigation Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
            ) {
                LargeButton(
                    text = if (pagerState.currentPage == 3) "Get Started" else "Next",
                    onClick = {
                        if (pagerState.currentPage < 3) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            viewModel.completeOnboarding(onComplete)
                        }
                    },
                    enabled = if (pagerState.currentPage == 0) state.userName.isNotBlank() else true
                )

                if (pagerState.currentPage > 0) {
                    LargeButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        text = "Back",
                        shape = RoundedCornerShape(theme.dimension.defaultRadius),
                        backgroundColor = theme.colors.surface,
                        contentColor = theme.colors.onSurface

                    )
                }
            }
            Spacer(modifier = Modifier.height(theme.dimension.bottomBarHeight))
        }
    }
}

@Composable
fun WelcomePage(
    userName: String,
    onUserNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hey there!",
                style = theme.typography.titleLarge,
                color = theme.colors.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = modifier.height(theme.dimension.veryLargeSpacing))
            Text(
                text = "What should we call you?",
                style = theme.typography.bodyLargeMedium,
                color = theme.colors.onSurface,
                textAlign = TextAlign.Center
            )
            InputTextField(
                value = userName,
                onValueChange = onUserNameChange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun FeaturePage(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = theme.dimension.veryLargeSpacing * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(theme.colors.surface)
                .border(borderStroke, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = theme.colors.primary.c50
            )
        }

        Spacer(modifier = Modifier.height(theme.dimension.veryLargeSpacing * 2))

        Text(
            text = title,
            style = theme.typography.titleMedium,
            color = theme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(theme.dimension.largeSpacing))

        Text(
            text = description,
            style = theme.typography.bodyLarge,
            color = theme.colors.greyScale.c50,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = theme.dimension.veryLargeSpacing)
        )
    }
}
