package com.example.anamwallet

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
import com.example.anamwallet.ui.components.BottomNavBar
import com.example.anamwallet.ui.components.Header
import com.example.anamwallet.ui.screens.activity.ActivityScreen
import com.example.anamwallet.ui.screens.asset.AssetScreen
import com.example.anamwallet.ui.screens.browser.BrowserScreen
import com.example.anamwallet.ui.screens.city.CityScreen
import com.example.anamwallet.ui.screens.main.MainScreen
import com.example.anamwallet.ui.theme.AnamwalletTheme

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
        topBar = { Header() },
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
            composable("Asset") {
                AssetScreen()
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