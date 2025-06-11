package com.anam.wallet.presentation.ui.screens.identity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anam.wallet.domain.repository.CredentialRepository
import com.anam.wallet.domain.usecase.credential.CreateVerifiablePresentationUseCase
import com.anam.wallet.domain.usecase.credential.IssueDriverLicenseUseCase
import com.anam.wallet.domain.usecase.identity.GetWalletStatusUseCase
import com.anam.wallet.domain.usecase.identity.InitializeWalletUseCase
import com.anam.wallet.model.identity.VerifiableCredential
import com.anam.wallet.model.identity.VerifiablePresentation
import com.anam.wallet.model.identity.WalletStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IdentityViewModel @Inject constructor(
    private val getWalletStatusUseCase: GetWalletStatusUseCase,
    private val initializeWalletUseCase: InitializeWalletUseCase,
    private val issueDriverLicenseUseCase: IssueDriverLicenseUseCase,
    private val createVerifiablePresentationUseCase: CreateVerifiablePresentationUseCase,
    private val credentialRepository: CredentialRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IdentityUiState())
    val uiState: StateFlow<IdentityUiState> = _uiState.asStateFlow()
    
    init {
        loadWalletStatus()
    }
    
    fun loadWalletStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val walletStatus = getWalletStatusUseCase()
                val vc = if (walletStatus.hasVC) credentialRepository.getStoredVC() else null
                
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
            
            initializeWalletUseCase("사용자")
                .onSuccess { walletInfo ->
                    _uiState.value = _uiState.value.copy(
                        isInitializing = false,
                        walletStatus = _uiState.value.walletStatus.copy(
                            isInitialized = true,
                            walletInfo = walletInfo
                        )
                    )
                    // Reload status to get updated information
                    loadWalletStatus()
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
            
            issueDriverLicenseUseCase()
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
            
            createVerifiablePresentationUseCase()
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