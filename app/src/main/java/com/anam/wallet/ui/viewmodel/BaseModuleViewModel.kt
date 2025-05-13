package com.anam.wallet.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.anam.wallet.ModuleManager
import com.anam.wallet.model.AccountInfo
import com.anam.wallet.model.BalanceSummary
import com.anam.wallet.model.ModuleInfo
import com.anam.wallet.model.NetworkInfo

/**
 * Base ViewModel class for handling module-related operations
 */
abstract class BaseModuleViewModel<T : BaseModuleState> : ViewModel() {

    /**
     * Get current UI state
     */
    abstract val uiState: T

    /**
     * Update UI state
     */
    abstract fun updateState(update: (T) -> T)

    /**
     * Creates a new instance of T with modified values
     */
    protected abstract fun createModifiedState(
        baseState: T,
        isLoading: Boolean = baseState.isLoading,
        progress: Int = baseState.progress,
        message: String = baseState.message,
        isSuccess: Boolean = baseState.isSuccess,
        moduleData: Map<String, ModuleData> = baseState.moduleData
    ): T

    /**
     * Download and load a module
     */
    suspend fun downloadModule(moduleManager: ModuleManager, moduleId: String) {
        try {
            updateState { state ->
                createModifiedState(
                    baseState = state,
                    isLoading = true,
                    progress = 0,
                    message = "Starting download...",
                    isSuccess = true
                )
            }

            moduleManager.downloadAndLoadModule(
                moduleId = moduleId,
                onProgress = { progress ->
                    updateState { state ->
                        createModifiedState(
                            baseState = state,
                            progress = progress
                        )
                    }
                },
                onComplete = { success, message ->
                    updateState { state ->
                        createModifiedState(
                            baseState = state,
                            isLoading = false,
                            message = message,
                            isSuccess = success
                        )
                    }
                    if (success) {
                        refreshModuleData(moduleManager)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, "Error downloading module", e)
            updateState { state ->
                createModifiedState(
                    baseState = state,
                    isLoading = false,
                    message = "Error: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }

    /**
     * Update the status message
     */
    fun updateMessage(message: String, isSuccess: Boolean) {
        updateState { state ->
            createModifiedState(
                baseState = state,
                message = message,
                isSuccess = isSuccess
            )
        }
    }

    /**
     * Clear status message
     */
    fun clearStatusMessage() {
        updateState { state ->
            createModifiedState(
                baseState = state,
                message = "",
                isSuccess = true
            )
        }
    }

    /**
     * Refresh module data
     */
    fun refreshModuleData(moduleManager: ModuleManager) {
        val moduleData = mutableMapOf<String, ModuleData>()

        moduleManager.getLoadedModuleIds().forEach { moduleId ->
            try {
                val moduleInfo = moduleManager.getFullModuleInfo(moduleId)
                if (moduleInfo != null) {
                    val accounts = moduleManager.getModuleAccounts(moduleId)
                    val networkInfo = moduleManager.getModuleNetworkInfo(moduleId)

                    // Get balance for the first account if available
                    val balanceSummary = if (!accounts.isNullOrEmpty()) {
                        moduleManager.getModuleBalanceSummary(moduleId, accounts.first().address)
                    } else {
                        null
                    }

                    moduleData[moduleId] = ModuleData(
                        moduleInfo = moduleInfo,
                        accounts = accounts ?: emptyList(),
                        networkInfo = networkInfo,
                        balanceSummary = balanceSummary
                    )
                }
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Error refreshing module data: $moduleId", e)
            }
        }

        updateState { state ->
            createModifiedState(
                baseState = state,
                moduleData = moduleData
            )
        }
    }

    /**
     * Unload a module
     */
    fun unloadModule(moduleManager: ModuleManager, moduleId: String) {
        try {
            val success = moduleManager.unloadModule(moduleId)

            if (success) {
                // Remove the module data from the UI state
                val updatedModuleData = uiState.moduleData.toMutableMap().apply {
                    remove(moduleId)
                }

                updateState { state ->
                    createModifiedState(
                        baseState = state,
                        message = "Module unloaded successfully",
                        isSuccess = true,
                        moduleData = updatedModuleData
                    )
                }
            } else {
                updateState { state ->
                    createModifiedState(
                        baseState = state,
                        message = "Failed to unload module",
                        isSuccess = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, "Error unloading module: $moduleId", e)
            updateState { state ->
                createModifiedState(
                    baseState = state,
                    message = "Error: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }
}

/**
 * Base state interface for module-related UI state
 */
interface BaseModuleState {
    val isLoading: Boolean
    val progress: Int
    val message: String
    val isSuccess: Boolean
    val moduleData: Map<String, ModuleData>
}

/**
 * Data class to hold module information for UI display
 */
data class ModuleData(
    val moduleInfo: ModuleInfo,
    val accounts: List<AccountInfo>,
    val networkInfo: NetworkInfo?,
    val balanceSummary: BalanceSummary?
)