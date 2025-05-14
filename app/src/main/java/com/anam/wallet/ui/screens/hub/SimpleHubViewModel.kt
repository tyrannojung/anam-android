package com.anam.wallet.ui.screens.hub

import com.anam.wallet.ui.viewmodel.SimpleModuleData
import com.anam.wallet.ui.viewmodel.SimpleModuleState
import com.anam.wallet.ui.viewmodel.SimpleModuleViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 단순화된 Hub 화면 ViewModel
 */
class SimpleHubViewModel : SimpleModuleViewModel<SimpleHubUiState>() {

    private val _uiState = MutableStateFlow(SimpleHubUiState())
    override val uiState: SimpleHubUiState
        get() = _uiState.value
    
    val uiStateFlow: StateFlow<SimpleHubUiState> = _uiState.asStateFlow()
    
    /**
     * 상태 업데이트
     */
    override fun updateState(update: (SimpleHubUiState) -> SimpleHubUiState) {
        _uiState.update(update)
    }
    
    /**
     * 수정된 상태 생성
     */
    override fun createModifiedState(
        baseState: SimpleHubUiState,
        isLoading: Boolean,
        progress: Int,
        message: String,
        isSuccess: Boolean,
        moduleData: Map<String, SimpleModuleData>,
        testResult: Map<String, String>
    ): SimpleHubUiState {
        return baseState.copy(
            isLoading = isLoading,
            progress = progress,
            message = message,
            isSuccess = isSuccess,
            moduleData = moduleData,
            testResult = testResult
        )
    }
}

/**
 * Hub 화면 UI 상태
 */
data class SimpleHubUiState(
    override val isLoading: Boolean = false,
    override val progress: Int = 0,
    override val message: String = "",
    override val isSuccess: Boolean = true,
    override val moduleData: Map<String, SimpleModuleData> = emptyMap(),
    override val testResult: Map<String, String> = emptyMap()
) : SimpleModuleState