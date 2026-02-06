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
import com.example.visualmoney.assetDetails.AssetDetailsScreen
import com.example.visualmoney.assetDetails.AssetDetailsViewModel
import com.example.visualmoney.calendar.CalendarScreen
import com.example.visualmoney.domain.model.toAsset
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
            NavHost(navController, startDestination = Routes.HOME) {
                composable(route = Routes.HOME) {

                    HomeScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        viewModel = homeViewModel, onGoToAssetDetails = {
                            symbol = it
                            navController.navigate(Routes.details(it))
                        },
                        onGoToBalance = {
                            navController.navigate(Routes.PORTFOLIO_OVERVIEW)
                        },
                        onGoToCalendar = { navController.navigate(Routes.CALENDAR) },
                        onNewAsset = { navController.navigate(Routes.NEW_ASSET) }
                    )
                }
                composable(route = Routes.CALENDAR) {
                    CalendarScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        onBack = { navController.popBackStack() })
                }
                composable(route = Routes.NEW_ASSET) {
                    val viewModel = koinViewModel<NewAssetViewModel>()

                    NewAssetScreen(
                        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                        onBack = { navController.popBackStack() },
                        viewModel = viewModel,
                        onNavigateToAssetDetails = {
                            println("Navigate to details of: $it")
                            symbol = it
                            navController.navigate(Routes.details(it))

                        },
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
    }
}
