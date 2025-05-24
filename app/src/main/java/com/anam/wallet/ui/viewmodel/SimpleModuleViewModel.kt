package com.anam.wallet.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.anam.wallet.SimpleModuleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Front Module 시스템을 위한 간소화된 모듈 뷰모델
 * 모듈 다운로드와 기본 상태 관리에 집중
 */
abstract class SimpleModuleViewModel<T : SimpleModuleState> : ViewModel() {

    /**
     * 현재 UI 상태
     */
    abstract val uiState: T
    
    /**
     * UI 상태 업데이트
     */
    abstract fun updateState(update: (T) -> T)
    
    /**
     * 새로운 상태 인스턴스 생성
     */
    protected abstract fun createModifiedState(
        baseState: T,
        isLoading: Boolean = baseState.isLoading,
        progress: Int = baseState.progress,
        message: String = baseState.message,
        isSuccess: Boolean = baseState.isSuccess,
        downloadedModules: Set<String> = baseState.downloadedModules
    ): T
    
    /**
     * 모듈 다운로드
     */
    suspend fun downloadModule(moduleManager: SimpleModuleManager, moduleId: String) {
        try {
            // 로딩 상태 업데이트
            updateState { state ->
                createModifiedState(
                    baseState = state,
                    isLoading = true,
                    progress = 0,
                    message = "다운로드 시작 중...",
                    isSuccess = true
                )
            }
            
            // 모듈 다운로드
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
                    // 완료 상태 업데이트
                    updateState { state ->
                        createModifiedState(
                            baseState = state,
                            isLoading = false,
                            message = message,
                            isSuccess = success,
                            downloadedModules = if (success) {
                                state.downloadedModules + moduleId
                            } else {
                                state.downloadedModules
                            }
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "모듈 다운로드 중 오류", e)
            updateState { state ->
                createModifiedState(
                    baseState = state,
                    isLoading = false,
                    message = "오류: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }
    
    /**
     * 상태 메시지 업데이트
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
     * 상태 메시지 초기화
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
     * 다운로드된 모듈 목록 새로고침
     */
    fun refreshDownloadedModules(moduleManager: SimpleModuleManager) {
        updateState { state ->
            createModifiedState(
                baseState = state,
                downloadedModules = moduleManager.getDownloadedModuleIds()
            )
        }
    }
    
    /**
     * 모듈 삭제
     */
    fun deleteModule(moduleManager: SimpleModuleManager, moduleId: String) {
        try {
            val success = moduleManager.deleteModule(moduleId)
            
            if (success) {
                updateState { state ->
                    createModifiedState(
                        baseState = state,
                        message = "모듈 삭제 성공",
                        isSuccess = true,
                        downloadedModules = state.downloadedModules - moduleId
                    )
                }
            } else {
                updateState { state ->
                    createModifiedState(
                        baseState = state,
                        message = "모듈 삭제 실패",
                        isSuccess = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "모듈 삭제 중 오류: $moduleId", e)
            updateState { state ->
                createModifiedState(
                    baseState = state,
                    message = "오류: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }
    
    companion object {
        private const val TAG = "SimpleModuleViewModel"
    }
}

/**
 * Front Module 시스템을 위한 간소화된 UI 상태
 */
interface SimpleModuleState {
    val isLoading: Boolean
    val progress: Int
    val message: String
    val isSuccess: Boolean
    val downloadedModules: Set<String>
}