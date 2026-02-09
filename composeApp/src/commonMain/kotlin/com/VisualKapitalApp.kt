package com

import AppDatabase
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.visualmoney.DarkBackgroundGradient
import com.example.visualmoney.LoadingOverlay
import com.example.visualmoney.SnackBarComponent
import com.example.visualmoney.assetDetails.AssetDetailsScreen
import com.example.visualmoney.assetDetails.AssetDetailsViewModel
import com.example.visualmoney.calendar.CalendarScreen
import com.example.visualmoney.calendar.CalendarScreenViewModel
import com.example.visualmoney.calendar.NewReminderScreen
import com.example.visualmoney.calendar.reminderBadgeColor
import com.example.visualmoney.calendar.showRemindersBadge
import com.example.visualmoney.home.HomeScreen
import com.example.visualmoney.home.HomeViewModel
import com.example.visualmoney.navigation.Routes
import com.example.visualmoney.newAsset.NewAssetScreen
import com.example.visualmoney.newAsset.NewAssetViewModel
import com.example.visualmoney.onboarding.OnboardingScreen
import com.example.visualmoney.onboarding.OnboardingViewModel
import com.example.visualmoney.portfolioOverview.PortfolioOverviewScreen
import com.example.visualmoney.premium.PremiumFeaturesScreen
import com.example.visualmoney.premium.PremiumViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VisualKapitalApp(navController: NavHostController = rememberNavController()) {
    var symbol by remember { mutableStateOf("") }
    var startDestination by remember { mutableStateOf<String?>(null) }
    
    // Check onboarding status and observe user name
    val database = org.koin.compose.koinInject<AppDatabase>()
    
    // Observe user name from database reactively
    val userNameState = database.onboardingPreferencesDao()
        .getOnboardingPreferences()
        .collectAsState(initial = null)
    
    val userName = userNameState.value?.userName?.ifBlank { "User" } ?: "User"
    
    LaunchedEffect(Unit) {
        val preferences = database.onboardingPreferencesDao().getOnboardingPreferencesOnce()
        startDestination = if (preferences?.isOnboardingCompleted == true) {
            Routes.HOME
        } else {
            Routes.ONBOARDING
        }
    }
    
    if (startDestination == null) {
        // Show loading while checking onboarding status
        Box(modifier = Modifier.fillMaxSize().background(DarkBackgroundGradient))
        return
    }
    
    Box(modifier = Modifier.background(DarkBackgroundGradient)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
        ) { paddingValues ->
            val homeViewModel = koinViewModel<HomeViewModel>()
            val calendarViewModel =
                koinViewModel<CalendarScreenViewModel>()
            NavHost(navController, startDestination = startDestination!!) {
                
                composable(route = Routes.ONBOARDING) {
                    val onboardingViewModel = koinViewModel<OnboardingViewModel>()
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onComplete = {
                            // Reload user name after onboarding
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                composable(route = Routes.HOME) {

                    HomeScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        viewModel = homeViewModel,
                        userName = userName,
                        onGoToAssetDetails = {
                            symbol = it
                            navController.navigate(Routes.details(it))
                        },
                        showCalendarBadge = calendarViewModel.state.showRemindersBadge,
                        calendarBadgeColor = calendarViewModel.state.reminderBadgeColor,
                        onGoToBalance = {
                            navController.navigate(Routes.PORTFOLIO_OVERVIEW)
                        },
                        onGoToCalendar = { navController.navigate(Routes.CALENDAR) },
                        onNewAsset = { navController.navigate(Routes.NEW_ASSET) },
                        onGoToPremium = { navController.navigate(Routes.PREMIUM_FEATURES) }
                    )
                }
                composable(route = Routes.CALENDAR) { backStackEntry ->
                    CalendarScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        viewModel = calendarViewModel,
                        onBack = { navController.popBackStack() },
                        onAddReminder = { navController.navigate(Routes.NEW_REMINDER) }
                    )
                }
                composable(route = Routes.NEW_REMINDER) {
                    NewReminderScreen(
                        calendarViewModel,
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
                    )
                }
                composable(route = Routes.NEW_ASSET) {
                    val viewModel = koinViewModel<NewAssetViewModel>()

                    NewAssetScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        onBack = { navController.popBackStack() },
                        viewModel = viewModel
                    )
                }
                composable(route = Routes.PORTFOLIO_OVERVIEW) { backStackEntry ->
                    PortfolioOverviewScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        viewModel = homeViewModel,
                        onBackPressed = { navController.popBackStack() })
                }
                composable(
                    route = Routes.DETAILS_ROUTE,
                    arguments = listOf(navArgument(Routes.DETAILS_SYMBOL) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val viewModel =
                        koinViewModel<AssetDetailsViewModel>(viewModelStoreOwner = backStackEntry)
                    viewModel.loadSymbolData(symbol)
                    AssetDetailsScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                            symbol = ""
                        })
                }
                
                composable(route = Routes.PREMIUM_FEATURES) {
                    val premiumViewModel = koinViewModel<PremiumViewModel>()
                    val purchases = com.revenuecat.purchases.kmp.Purchases.sharedInstance
                    PremiumFeaturesScreen(
                        viewModel = premiumViewModel,
                        purchases = purchases,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAsset = { symbol ->
                            navController.navigate(Routes.details(symbol))
                        },
                        onPurchaseComplete = {
                            // Stay on the screen to show premium dashboard
                        }
                    )
                }
            }
        }
        LoadingOverlay(modifier = Modifier.fillMaxSize())
        SnackBarComponent()

    }
}
