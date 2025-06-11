package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import com.anam.wallet.model.miniapp.MiniAppManifest

/**
 * 미니앱 로더 - 미니앱 로드 및 매니페스트 읽기 전용
 * 
 * 파일 작업은 MiniAppFileManager에 위임합니다.
 */
class MiniAppLoader(private val context: Context) {
    
    companion object {
        private const val TAG = "MiniAppLoader"
    }
    
    private val fileManager = MiniAppFileManager(context)
    
    /**
     * 미니앱을 로드합니다.
     * 설치되어 있지 않으면 자동으로 설치를 시도합니다.
     */
    suspend fun loadMiniApp(appId: String): MiniAppManifest? {
        return try {
            // 1. 설치 및 무결성 확인
            if (!fileManager.isAppInstalled(appId) || !fileManager.validateAppIntegrity(appId)) {
                Log.d(TAG, "App $appId not installed or invalid, attempting to install...")
                
                // 2. 자동 설치 시도 (폴백)
                if (!fileManager.installFromAssets(appId)) {
                    Log.e(TAG, "Failed to install app: $appId")
                    return null
                }
            }
            
            // 3. 매니페스트 로드
            fileManager.loadManifest(appId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load mini app: $appId", e)
            null
        }
    }
    
    /**
     * 미니앱의 기본 경로를 반환합니다. (WebView 로드용)
     */
    fun getMiniAppBasePath(appId: String): String {
        return fileManager.getAppBasePath(appId)
    }
    
    /**
     * 미니앱의 아이콘 경로를 반환합니다.
     */
    fun getMiniAppIconPath(appId: String, iconPath: String?): String? {
        return fileManager.getAppIconPath(appId, iconPath)
    }
}