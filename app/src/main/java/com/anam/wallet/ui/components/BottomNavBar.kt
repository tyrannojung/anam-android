package com.anam.wallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavBar(
    navController: NavController,
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val screens = listOf("Main", "City", "Asset", "Browser", "Activity")

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        when (screen) {
                            "Main" -> Icons.Default.Home
                            "City" -> Icons.Default.LocationCity
                            "Asset" -> Icons.Default.AccountBalance
                            "Browser" -> Icons.Default.Language
                            else -> Icons.Default.Insights
                        },
                        contentDescription = screen
                    )
                },
                label = { Text(screen) },
                selected = screen == currentScreen,
                onClick = {
                    onScreenSelected(screen)
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}