package com.anam.wallet.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.anam.wallet.SimpleModuleManager
import com.anam.wallet.model.AccountInfo
import com.anam.wallet.model.ModuleInfo
import com.anam.wallet.model.NetworkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 간소화된 모듈 뷰모델
 * SimpleModuleManager와 함께 사용
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
        moduleData: Map<String, SimpleModuleData> = baseState.moduleData,
        testResult: Map<String, String> = baseState.testResult
    ): T
    
    /**
     * 모듈 다운로드 및 로드
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
            
            // 모듈 다운로드 및 로드
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
                            isSuccess = success
                        )
                    }
                    
                    // 성공 시 모듈 데이터 새로고침 및 테스트 결과 추가
                    if (success) {
                        refreshModuleData(moduleManager)
                        
                        // 모듈 테스트 결과 가져오기
                        val testResult = moduleManager.getModuleSummary(moduleId)
                        updateState { state ->
                            createModifiedState(
                                baseState = state,
                                testResult = testResult
                            )
                        }
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
     * 모듈 데이터 새로고침
     */
    fun refreshModuleData(moduleManager: SimpleModuleManager) {
        val moduleData = mutableMapOf<String, SimpleModuleData>()
        
        moduleManager.getLoadedModuleIds().forEach { moduleId ->
            try {
                val moduleInfo = moduleManager.getModuleInfo(moduleId)
                if (moduleInfo != null) {
                    val accounts = moduleManager.getModuleAccounts(moduleId)
                    val networkInfo = moduleManager.getNetworkInfo(moduleId)
                    
                    moduleData[moduleId] = SimpleModuleData(
                        moduleInfo = moduleInfo,
                        accounts = accounts ?: emptyList(),
                        networkInfo = networkInfo
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "모듈 데이터 새로고침 중 오류: $moduleId", e)
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
     * 모듈 언로드
     */
    fun unloadModule(moduleManager: SimpleModuleManager, moduleId: String) {
        try {
            val success = moduleManager.unloadModule(moduleId)
            
            if (success) {
                // UI 상태에서 모듈 데이터 제거
                val updatedModuleData = uiState.moduleData.toMutableMap().apply {
                    remove(moduleId)
                }
                
                updateState { state ->
                    createModifiedState(
                        baseState = state,
                        message = "모듈 언로드 성공",
                        isSuccess = true,
                        moduleData = updatedModuleData,
                        testResult = emptyMap()
                    )
                }
            } else {
                updateState { state ->
                    createModifiedState(
                        baseState = state,
                        message = "모듈 언로드 실패",
                        isSuccess = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "모듈 언로드 중 오류: $moduleId", e)
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
 * 모듈 UI 상태 인터페이스
 */
interface SimpleModuleState {
    val isLoading: Boolean
    val progress: Int
    val message: String
    val isSuccess: Boolean
    val moduleData: Map<String, SimpleModuleData>
    val testResult: Map<String, String>
}

/**
 * 모듈 데이터 클래스
 */
data class SimpleModuleData(
    val moduleInfo: ModuleInfo,
    val accounts: List<AccountInfo>,
    val networkInfo: NetworkInfo?
)