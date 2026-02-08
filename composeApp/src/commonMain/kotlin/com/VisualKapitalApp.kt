package com

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.example.visualmoney.portfolioOverview.PortfolioOverviewScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VisualKapitalApp(navController: NavHostController = rememberNavController()) {
    var symbol by remember { mutableStateOf("") }
    Box(modifier = Modifier.background(DarkBackgroundGradient)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
        ) { paddingValues ->
            val homeViewModel = koinViewModel<HomeViewModel>()
            val calendarViewModel =
                koinViewModel<CalendarScreenViewModel>()
            NavHost(navController, startDestination = Routes.HOME) {

                composable(route = Routes.HOME) {

                    HomeScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        viewModel = homeViewModel, onGoToAssetDetails = {
                            symbol = it
                            navController.navigate(Routes.details(it))
                        },
                        showCalendarBadge = calendarViewModel.state.showRemindersBadge,
                        calendarBadgeColor = calendarViewModel.state.reminderBadgeColor,
                        onGoToBalance = {
                            navController.navigate(Routes.PORTFOLIO_OVERVIEW)
                        },
                        onGoToCalendar = { navController.navigate(Routes.CALENDAR) },
                        onNewAsset = { navController.navigate(Routes.NEW_ASSET) }
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
            }
        }
        LoadingOverlay(modifier = Modifier.fillMaxSize())
        SnackBarComponent()

    }
}
