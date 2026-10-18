package com.example.shopeefooddriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shopeefooddriver.auth.LoginScreen
import com.example.shopeefooddriver.data.FirebaseRepository
import com.example.shopeefooddriver.ui.theme.ShopeeFoodDriverTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopeeFoodDriverTheme {
                AuthGate()
            }
        }
    }
}

@Composable
fun AuthGate() {
    var isLoggedIn by remember { mutableStateOf(FirebaseRepository.isLoggedIn()) }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (isLoggedIn) {
        ShopeeFoodDriverApp(
            onLogout = {
                FirebaseRepository.logout()
                isLoggedIn = false
            }
        )
    } else {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Orders : Screen("orders", "Nhận đơn", Icons.Default.LocalShipping)
    data object Income : Screen("income", "Thu nhập", Icons.Default.AttachMoney)
    data object Wallet : Screen("wallet", "Ví", Icons.Default.AccountBalanceWallet)
    data object Notifications : Screen("notifications", "Thông báo", Icons.Default.Notifications)
    data object Profile : Screen("profile", "Tôi", Icons.Default.Person)
}

@Composable
fun ShopeeFoodDriverApp(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Orders,
        Screen.Income,
        Screen.Wallet,
        Screen.Notifications,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Orders.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Orders.route) { OrdersScreen() }
            composable(Screen.Income.route) { IncomeScreen() }
            composable(Screen.Wallet.route) { WalletScreen() }
            composable(Screen.Notifications.route) { NotificationsScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onLogout = onLogout) }
        }
    }
}
