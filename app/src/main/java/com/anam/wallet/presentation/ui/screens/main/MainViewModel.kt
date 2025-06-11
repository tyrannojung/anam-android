package com.anam.wallet.presentation.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam.wallet.domain.usecase.miniapp.ScanInstalledAppsUseCase
import com.anam.wallet.presentation.miniapp.MiniAppManager
import com.anam.wallet.model.miniapp.ScannedMiniApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = false,
    val miniApps: List<ScannedMiniApp> = emptyList(),
    val activeBlockchainId: String? = null,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val scanInstalledAppsUseCase: ScanInstalledAppsUseCase,
    private val miniAppManager: MiniAppManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        loadMiniApps()
    }
    
    fun loadMiniApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            scanInstalledAppsUseCase()
                .onSuccess { apps ->
                    _uiState.update { 
                        it.copy(
                            miniApps = apps,
                            isLoading = false
                        )
                    }
                    
                    // Activate first blockchain if available
                    val firstBlockchain = apps.firstOrNull { it.type == "blockchain" }
                    firstBlockchain?.let { 
                        activateBlockchain(it.appId)
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun activateBlockchain(blockchainId: String) {
        viewModelScope.launch {
            try {
                miniAppManager.activateBlockchain(blockchainId)
                _uiState.update { 
                    it.copy(activeBlockchainId = blockchainId)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to activate blockchain: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}