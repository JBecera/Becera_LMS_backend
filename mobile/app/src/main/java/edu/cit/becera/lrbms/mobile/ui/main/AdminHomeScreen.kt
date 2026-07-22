package edu.cit.becera.lrbms.mobile.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.cit.becera.lrbms.mobile.ui.account.AccountScreen
import edu.cit.becera.lrbms.mobile.ui.admin.AdminDashboardScreen
import edu.cit.becera.lrbms.mobile.ui.admin.ManageAccountsScreen

private data class AdminTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val adminTabs = listOf(
    AdminTab("dashboard", "Home", Icons.Default.Dashboard),
    AdminTab("accounts", "Accounts", Icons.Default.ManageAccounts),
    AdminTab("account", "My Account", Icons.Default.Person)
)

@Composable
fun AdminHomeScreen(onSignedOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                adminTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
            composable("dashboard") { AdminDashboardScreen() }
            composable("accounts") { ManageAccountsScreen() }
            composable("account") { AccountScreen(onSignedOut = onSignedOut) }
        }
    }
}
