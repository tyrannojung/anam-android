package com.anam.wallet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anam.wallet.ui.components.BottomNavBar
import com.anam.wallet.ui.components.Header
import com.anam.wallet.ui.screens.activity.ActivityScreen
import com.anam.wallet.ui.screens.hub.SimpleHubScreen
import com.anam.wallet.ui.screens.browser.BrowserScreen
import com.anam.wallet.ui.screens.city.CityScreen
import com.anam.wallet.ui.screens.main.MainScreen
import com.anam.wallet.ui.screens.moduledetail.ModuleDetailScreen
import com.anam.wallet.ui.theme.AnamwalletTheme

private const val TAG = "MainActivity"

// Create a CompositionLocal for NavController
val LocalNavController = staticCompositionLocalOf<NavController> { 
    error("NavController not provided") 
}

// Create a shared/singleton moduleManager that persists across navigation
val moduleManager by lazy {
    // Will be properly initialized in WalletApp
    SimpleModuleManager(null)
}

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
    val context = LocalContext.current
    
    // Initialize the moduleManager with context if not already initialized
    if (moduleManager.context == null) {
        moduleManager.initializeContext(context)
        Log.d(TAG, "SimpleModuleManager initialized with context")
    }

    CompositionLocalProvider(LocalNavController provides navController) {
        Scaffold(
            topBar = { 
                // Don't show header for Browser screen or Detail screen
                if (currentScreen != "Browser" && currentScreen != "Detail") {
                    Header() 
                }
            },
            bottomBar = {
                // Don't show bottom navigation for Detail screen
                if (currentScreen != "Detail") {
                    BottomNavBar(
                        navController = navController,
                        currentScreen = currentScreen,
                        onScreenSelected = { screen ->
                            currentScreen = screen
                        }
                    )
                }
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
                    // Use the shared moduleManager
                    SimpleHubScreen(moduleManager = moduleManager)
                }
                composable("Browser") {
                    BrowserScreen()
                }
                composable("Activity") {
                    ActivityScreen()
                }
                
                // Add module detail screen navigation
                composable(
                    route = "moduleDetail/{moduleId}",
                    arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
                    
                    Log.d(TAG, "Entering ModuleDetailScreen for moduleId: $moduleId")
                    
                    // Use the shared moduleManager
                    ModuleDetailScreen(
                        moduleId = moduleId,
                        moduleManager = moduleManager,
                        onBackClick = { 
                            Log.d(TAG, "Back button pressed from ModuleDetailScreen")
                            navController.popBackStack() 
                        }
                    )
                    
                    // Update currentScreen to hide bottom navigation and header
                    DisposableEffect(Unit) {
                        val prevScreen = currentScreen
                        currentScreen = "Detail"
                        onDispose {
                            Log.d(TAG, "Leaving ModuleDetailScreen, restoring screen: $prevScreen")
                            currentScreen = prevScreen
                        }
                    }
                }
            }
        }
    }
}