package com.example.finance.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finance.feature.dashboard.DashboardScreen
import com.example.finance.feature.goals.GoalsScreen
import com.example.finance.feature.insights.InsightsScreen
import com.example.finance.feature.transaction.TransactionScreen

sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Insights     : Screen("insights")
    object Goals        : Screen("goals")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController    = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route)    { DashboardScreen(navController) }
        composable(Screen.Transactions.route) { TransactionScreen(navController) }
        composable(Screen.Insights.route)     { InsightsScreen(navController) }
        composable(Screen.Goals.route)        { GoalsScreen(navController) }
    }
}