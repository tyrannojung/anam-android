package com.anam.wallet.presentation.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val appVersion: String = "1.0.0",
    val isNotificationEnabled: Boolean = true,
    val isBiometricEnabled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun toggleNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isNotificationEnabled = !_uiState.value.isNotificationEnabled
            )
            // TODO: Save to preferences
        }
    }
    
    fun toggleBiometric() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBiometricEnabled = !_uiState.value.isBiometricEnabled
            )
            // TODO: Save to preferences and setup biometric
        }
    }
}