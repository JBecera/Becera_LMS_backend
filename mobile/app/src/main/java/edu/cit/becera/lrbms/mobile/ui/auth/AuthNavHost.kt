package edu.cit.becera.lrbms.mobile.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.becera.lrbms.mobile.ui.main.MemberHomeScreen

@Composable
fun AuthNavHost(startLoggedIn: Boolean) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = if (startLoggedIn) "home" else "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToDashboard = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("register") {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }
        composable("home") {
            MemberHomeScreen(
                onSignedOut = { navController.navigate("login") { popUpTo("home") { inclusive = true } } }
            )
        }
    }
}
