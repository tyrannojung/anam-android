package com.anam.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anam.wallet.ui.components.BottomNavBar
import com.anam.wallet.ui.components.Header
import com.anam.wallet.ui.screens.activity.ActivityScreen
import com.anam.wallet.ui.screens.hub.HubScreen
import com.anam.wallet.ui.screens.browser.BrowserScreen
import com.anam.wallet.ui.screens.city.CityScreen
import com.anam.wallet.ui.screens.main.MainScreen
import com.anam.wallet.ui.theme.AnamwalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnamwalletTheme {
                WalletApp()
            }
        }
    }
}

@Composable
fun WalletApp() {
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf("Main") }

    Scaffold(
        topBar = { 
            // Don't show header for Browser screen
            if (currentScreen != "Browser") {
                Header() 
            }
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreen = screen
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Main") {
                MainScreen()
            }
            composable("City") {
                CityScreen()
            }
            composable("Hub") {
                HubScreen()
            }
            composable("Browser") {
                BrowserScreen()
            }
            composable("Activity") {
                ActivityScreen()
            }
        }
    }
}