package com.jobautoapply.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jobautoapply.app.automation.AutomationManager
import com.jobautoapply.app.data.repository.AppPreferences
import com.jobautoapply.app.ui.navigation.Screen
import com.jobautoapply.app.ui.screens.DashboardScreen
import com.jobautoapply.app.ui.screens.HistoryScreen
import com.jobautoapply.app.ui.screens.ProfileScreen
import com.jobautoapply.app.ui.screens.SettingsScreen
import com.jobautoapply.app.ui.screens.WebViewAutomationScreen
import com.jobautoapply.app.ui.theme.DarkCardBorder
import com.jobautoapply.app.ui.theme.DarkSurface
import com.jobautoapply.app.ui.theme.JobAutoApplyTheme
import com.jobautoapply.app.ui.theme.PrimaryBlue
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appPreferences = AppPreferences(applicationContext)
        val automationManager = AutomationManager(applicationContext, appPreferences)

        setContent {
            JobAutoApplyTheme {
                MainApp(appPreferences, automationManager)
            }
        }
    }
}

@Composable
fun MainApp(
    appPreferences: AppPreferences,
    automationManager: AutomationManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val uiState by automationManager.uiState.collectAsState()

    val screens = listOf(
        Screen.Dashboard,
        Screen.Automation,
        Screen.Profile,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = androidx.compose.ui.unit.Dp(0f)
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 10.sp) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    state = uiState,
                    automationManager = automationManager,
                    onNavigateToBot = {
                        navController.navigate(Screen.Automation.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Automation.route) {
                WebViewAutomationScreen(
                    state = uiState,
                    automationManager = automationManager
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(appPreferences = appPreferences)
            }
            composable(Screen.History.route) {
                HistoryScreen(appPreferences = appPreferences)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(appPreferences = appPreferences)
            }
        }
    }
}
