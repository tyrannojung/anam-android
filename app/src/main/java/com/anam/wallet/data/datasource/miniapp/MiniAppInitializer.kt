package com.anam.wallet.data.datasource.miniapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 스플래시 화면에서 미니앱 초기화를 담당하는 헬퍼 클래스
 * 
 * 최초 앱 실행 시 내장된 미니앱들을 설치합니다.
 * 실제 파일 작업은 MiniAppFileManager에 위임합니다.
 */
class MiniAppInitializer(private val context: Context) {
    
    companion object {
        private const val TAG = "MiniAppInitializer"
    }
    
    private val fileManager = MiniAppFileManager(context)
    
    /**
     * 내장된 미니앱들을 초기화합니다.
     * 
     * @param onProgress 진행 상황 콜백 (현재 개수, 전체 개수)
     * @return 초기화 성공 여부
     */
    suspend fun initializeBuiltInMiniApps(
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting mini app initialization...")
        
        // MiniAppFileManager에 위임
        val result = fileManager.installAllFromAssets(onProgress)
        
        if (result) {
            Log.d(TAG, "Mini app initialization completed")
        } else {
            Log.e(TAG, "Mini app initialization failed")
        }
        
        result
    }
}