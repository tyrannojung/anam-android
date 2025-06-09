package com.anam.wallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.anam.wallet.miniapp.MiniAppInitializer
import com.anam.wallet.ui.screens.splash.SplashScreen
import com.anam.wallet.ui.theme.AnamwalletTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AnamwalletTheme {
                var progress by remember { mutableStateOf(0f) }
                var statusText by remember { mutableStateOf("Initializing...") }
                var isInitialized by remember { mutableStateOf(false) }
                
                SplashScreen(
                    progress = progress,
                    statusText = statusText,
                    onInitializationComplete = {
                        if (!isInitialized) {
                            isInitialized = true
                            // Navigate to MainActivity
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                )
                
                // Run initialization in background
                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        try {
                            statusText = "Checking resources..."
                            progress = 0.1f
                            delay(300)
                            
                            statusText = "Loading mini apps..."
                            progress = 0.3f
                            
                            Log.d("SplashActivity", "Starting mini app initialization...")
                            val initializer = MiniAppInitializer(this@SplashActivity)
                            initializer.initializeBuiltInMiniApps { current, total ->
                                val initProgress = 0.3f + (current.toFloat() / total) * 0.5f
                                progress = initProgress
                                statusText = "Loading mini apps... ($current/$total)"
                                Log.d("SplashActivity", "Progress: $current/$total")
                            }
                            
                            statusText = "Preparing wallet..."
                            progress = 0.9f
                            delay(300)
                            
                            statusText = "Complete!"
                            progress = 1f
                            
                            Log.d("SplashActivity", "Initialization complete")
                        } catch (e: Exception) {
                            Log.e("SplashActivity", "Failed to initialize mini apps", e)
                            statusText = "Error initializing..."
                            delay(1000)
                            progress = 1f // Still proceed to main activity
                        }
                    }
                }
            }
        }
    }
}