package com.anam.wallet.ui.screens.identity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam.wallet.model.identity.VerifiableCredential
import com.anam.wallet.model.identity.VerifiablePresentation
import com.anam.wallet.service.DIDService
import com.anam.wallet.service.WalletStatus
import com.anam.wallet.storage.VCManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IdentityViewModel(context: Context) : ViewModel() {
    
    private val didService = DIDService(context)
    private val vcManager = VCManager(context)
    
    private val _uiState = MutableStateFlow(IdentityUiState())
    val uiState: StateFlow<IdentityUiState> = _uiState.asStateFlow()
    
    fun loadWalletStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val walletStatus = didService.getWalletStatus()
                val vc = if (walletStatus.hasVC) vcManager.getVC() else null
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    walletStatus = walletStatus,
                    verifiableCredential = vc
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun initializeWallet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isInitializing = true, error = null)
            
            didService.initializeWallet("사용자")
                .onSuccess { walletInfo ->
                    _uiState.value = _uiState.value.copy(
                        isInitializing = false,
                        walletStatus = _uiState.value.walletStatus.copy(
                            isInitialized = true,
                            walletInfo = walletInfo
                        )
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isInitializing = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun issueDriverLicense() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isIssuingLicense = true, error = null)
            
            didService.issueDriverLicense()
                .onSuccess { vc ->
                    _uiState.value = _uiState.value.copy(
                        isIssuingLicense = false,
                        verifiableCredential = vc,
                        walletStatus = _uiState.value.walletStatus.copy(hasVC = true)
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isIssuingLicense = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun createVP() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingVP = true, error = null)
            
            didService.createVerifiablePresentation()
                .onSuccess { vp ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingVP = false,
                        verifiablePresentation = vp
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingVP = false,
                        error = exception.message
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class IdentityUiState(
    val isLoading: Boolean = false,
    val isInitializing: Boolean = false,
    val isIssuingLicense: Boolean = false,
    val isCreatingVP: Boolean = false,
    val walletStatus: WalletStatus = WalletStatus(
        isInitialized = false,
        hasVC = false,
        hasSecureKey = false,
        walletInfo = null
    ),
    val verifiableCredential: VerifiableCredential? = null,
    val verifiablePresentation: VerifiablePresentation? = null,
    val error: String? = null
)