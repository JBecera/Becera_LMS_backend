package edu.cit.becera.lrbms.mobile.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.ui.main.AdminHomeScreen
import edu.cit.becera.lrbms.mobile.ui.main.LibrarianHomeScreen
import edu.cit.becera.lrbms.mobile.ui.main.MemberHomeScreen

private fun homeRouteForRole(role: String?): String = when (role?.uppercase()) {
    "LIBRARIAN" -> "librarian-home"
    "ADMIN" -> "admin-home"
    else -> "member-home"
}

@Composable
fun AuthNavHost(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startDestination = if (startLoggedIn) homeRouteForRole(SessionManager.current?.role) else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToDashboard = {
                    val target = homeRouteForRole(SessionManager.current?.role)
                    navController.navigate(target) { popUpTo("login") { inclusive = true } }
                }
            )
        }
        composable("register") {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }
        composable("member-home") {
            MemberHomeScreen(
                onSignedOut = { navController.navigate("login") { popUpTo("member-home") { inclusive = true } } }
            )
        }
        composable("librarian-home") {
            LibrarianHomeScreen(
                onSignedOut = { navController.navigate("login") { popUpTo("librarian-home") { inclusive = true } } }
            )
        }
        composable("admin-home") {
            AdminHomeScreen(
                onSignedOut = { navController.navigate("login") { popUpTo("admin-home") { inclusive = true } } }
            )
        }
    }
}
