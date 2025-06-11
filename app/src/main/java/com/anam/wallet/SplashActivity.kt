package com.anam.wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anam.wallet.presentation.ui.screens.splash.SplashScreen
import com.anam.wallet.presentation.ui.screens.splash.SplashViewModel
import com.anam.wallet.presentation.ui.theme.AnamwalletTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    
    private val viewModel: SplashViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AnamwalletTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                
                SplashScreen(
                    progress = uiState.progress,
                    statusText = uiState.statusText,
                    onInitializationComplete = {
                        // Handled by LaunchedEffect below
                    }
                )
                
                // Navigate when initialization is complete
                LaunchedEffect(uiState.isInitialized) {
                    if (uiState.isInitialized) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}