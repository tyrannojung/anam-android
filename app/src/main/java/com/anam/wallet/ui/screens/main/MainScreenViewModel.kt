package com.anam.wallet.ui.screens.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 단순화된 MainScreen ViewModel
 */
class MainScreenViewModel : ViewModel() {
    // UI 상태
    private val _uiStateFlow = MutableStateFlow(MainScreenUiState())
    val uiStateFlow: StateFlow<MainScreenUiState> = _uiStateFlow.asStateFlow()
}

/**
 * MainScreen UI 상태
 */
data class MainScreenUiState(
    // 상태 필드가 필요하면 여기에 추가
    val message: String = ""
)