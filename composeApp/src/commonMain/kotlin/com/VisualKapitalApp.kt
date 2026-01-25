package com

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.visualmoney.calendar.CalendarScreen
import com.example.visualmoney.home.HomeScreen
import com.example.visualmoney.home.theme
import com.example.visualmoney.navigation.VisualKapitalScreens

@Composable
fun VisualKapitalApp(navController: NavHostController = rememberNavController()) {

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Get the name of the current screen
    val currentScreen = VisualKapitalScreens.valueOf(
        backStackEntry?.destination?.route ?: VisualKapitalScreens.Home.name
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = theme.colors.surface,
    ) {
        NavHost(navController, startDestination = VisualKapitalScreens.Home) {
            composable(route = VisualKapitalScreens.Home.name) {
                HomeScreen(
                    onGoToCalendar = { navController.navigate(VisualKapitalScreens.Calendar.name) }
                )
            }
            composable(route = VisualKapitalScreens.Calendar.name) { CalendarScreen(onBack = { navController.popBackStack() }) }
//            composable(route = VisualKapitalScreens.News.name) {  }
        }
    }
}