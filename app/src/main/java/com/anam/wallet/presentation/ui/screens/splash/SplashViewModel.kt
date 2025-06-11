package com.anam.wallet.presentation.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam.wallet.domain.usecase.miniapp.InitializeMiniAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val initializeMiniAppsUseCase: InitializeMiniAppsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        initializeMiniApps()
    }
    
    private fun initializeMiniApps() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    statusText = "Checking resources...",
                    progress = 0.1f
                )
                
                kotlinx.coroutines.delay(300)
                
                _uiState.value = _uiState.value.copy(
                    statusText = "Loading mini apps...",
                    progress = 0.3f
                )
                
                initializeMiniAppsUseCase { current, total ->
                    val initProgress = 0.3f + (current.toFloat() / total) * 0.5f
                    _uiState.value = _uiState.value.copy(
                        progress = initProgress,
                        statusText = "Loading mini apps... ($current/$total)"
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    statusText = "Preparing wallet...",
                    progress = 0.9f
                )
                
                kotlinx.coroutines.delay(300)
                
                _uiState.value = _uiState.value.copy(
                    statusText = "Complete!",
                    progress = 1f,
                    isInitialized = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusText = "Error initializing...",
                    progress = 1f,
                    error = e.message,
                    isInitialized = true // Still proceed to main activity
                )
            }
        }
    }
}

data class SplashUiState(
    val progress: Float = 0f,
    val statusText: String = "Initializing...",
    val isInitialized: Boolean = false,
    val error: String? = null
)