package edu.cit.becera.lrbms.mobile.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
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
import edu.cit.becera.lrbms.mobile.ui.borrowing.MyBorrowingScreen
import edu.cit.becera.lrbms.mobile.ui.catalog.CatalogScreen
import edu.cit.becera.lrbms.mobile.ui.dashboard.DashboardScreen
import edu.cit.becera.lrbms.mobile.ui.fines.FinesScreen
import edu.cit.becera.lrbms.mobile.ui.reservations.ReservationsScreen

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("dashboard", "Home", Icons.Default.Home),
    Tab("catalog", "Catalog", Icons.AutoMirrored.Filled.MenuBook),
    Tab("my-borrowing", "Borrowing", Icons.Default.CollectionsBookmark),
    Tab("reservations", "Reserved", Icons.Default.Bookmark),
    Tab("fines", "Fines", Icons.Default.Payments),
    Tab("account", "Account", Icons.Default.Person)
)

@Composable
fun MemberHomeScreen(onSignedOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                tabs.forEach { tab ->
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
            composable("dashboard") { DashboardScreen() }
            composable("catalog") { CatalogScreen() }
            composable("my-borrowing") { MyBorrowingScreen() }
            composable("reservations") { ReservationsScreen() }
            composable("fines") { FinesScreen() }
            composable("account") { AccountScreen(onSignedOut = onSignedOut) }
        }
    }
}
