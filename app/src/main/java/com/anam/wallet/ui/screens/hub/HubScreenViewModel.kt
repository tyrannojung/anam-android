package com.anam.wallet.ui.screens.hub

import com.anam.wallet.ui.viewmodel.BaseModuleState
import com.anam.wallet.ui.viewmodel.BaseModuleViewModel
import com.anam.wallet.ui.viewmodel.ModuleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the HubScreen
 */
class HubScreenViewModel : BaseModuleViewModel<HubScreenUiState>() {

    private val _uiState = MutableStateFlow(HubScreenUiState())
    override val uiState: HubScreenUiState
        get() = _uiState.value

    val uiStateFlow: StateFlow<HubScreenUiState> = _uiState.asStateFlow()

    /**
     * Update the state
     */
    override fun updateState(update: (HubScreenUiState) -> HubScreenUiState) {
        _uiState.update(update)
    }

    /**
     * Create a new state with modified values
     */
    override fun createModifiedState(
        baseState: HubScreenUiState,
        isLoading: Boolean,
        progress: Int,
        message: String,
        isSuccess: Boolean,
        moduleData: Map<String, ModuleData>
    ): HubScreenUiState {
        return baseState.copy(
            isLoading = isLoading,
            progress = progress,
            message = message,
            isSuccess = isSuccess,
            moduleData = moduleData
        )
    }
}

/**
 * UI state for the HubScreen
 */
data class HubScreenUiState(
    override val isLoading: Boolean = false,
    override val progress: Int = 0,
    override val message: String = "",
    override val isSuccess: Boolean = true,
    override val moduleData: Map<String, ModuleData> = emptyMap()
) : BaseModuleState