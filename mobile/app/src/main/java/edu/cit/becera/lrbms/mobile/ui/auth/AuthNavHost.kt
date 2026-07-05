package edu.cit.becera.lrbms.mobile.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.becera.lrbms.mobile.ui.dashboard.DashboardScreen

@Composable
fun AuthNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToDashboard = { navController.navigate("dashboard") }
            )
        }
        composable("register") {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }
        composable("dashboard") {
            DashboardScreen(onLogout = { navController.navigate("login") { popUpTo("login") { inclusive = true } } })
        }
    }
}
