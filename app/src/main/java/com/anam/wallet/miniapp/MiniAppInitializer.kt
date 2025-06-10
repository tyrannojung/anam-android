package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * 미니앱 초기화 담당 - assets에서 filesDir로 복사 및 압축 해제
 */
class MiniAppInitializer(private val context: Context) {

    // companion object 클래스 인스턴스 생성 없이 바로 사용 가능
    // 인스턴스 없이 바로 사용
    // println(MiniAppInitializer.TAG)  // "MiniAppInitializer"
    companion object {
        private const val TAG = "MiniAppInitializer"
        private const val MINI_APPS_DIR = "miniapps"
        private const val INITIALIZED_KEY = "miniapps_initialized"
        private const val PREFS_NAME = "miniapp_prefs"
    }
    
    /**
     * 내장 미니앱들을 초기화 (assets → filesDir)
     */

    // suspend: 코루틴 함수 (비동기 실행 가능)
    // withContext(Dispatchers.IO) --> 전체 함수가 IO 스레드에서 실행
    // withContext = "이 컨텍스트(스레드)에서 실행해줘"
    suspend fun initializeBuiltInMiniApps(
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        // SharedPreferences
        // 앱의 간단한 설정값을 저장하는 곳
        val prefs = context.getSharedPreferences(
            PREFS_NAME, // 파일 이름
            Context.MODE_PRIVATE // 접근 권한 (이 앱만 사용)
        )
        val isInitialized = prefs.getBoolean(INITIALIZED_KEY, false)
        // --> 저장된 값이 없으면 2번째 인자인 기본값 false를 반환
        
        if (isInitialized) {
            Log.d(TAG, "Mini apps already initialized, skipping...")
            return@withContext // 그냥 빠져나가기만 함, 함수 전체 종료 X
            // return  // ❌ 컴파일 에러!
        }
        
        try {
            // assets 폴더에 접근할 수 있는 관리자
            val assetManager = context.assets
            val miniappFiles = assetManager.list(MINI_APPS_DIR) ?: emptyArray()
            val zipFiles = miniappFiles.filter { it.endsWith(".zip") }
            
            Log.d(TAG, "Found ${zipFiles.size} mini app ZIP files to initialize")
            
            zipFiles.forEachIndexed { index, zipFileName ->
                try {
                    Log.d(TAG, "Initializing: $zipFileName")
                    onProgress(index + 1, zipFiles.size)
                    
                    // Extract app ID from filename (e.g., com.anam.ethereum_1.0.0.zip)
                    val appId = zipFileName.substringBefore("_")
                    
                    // Check if already extracted
                    // context.filesDir = 부모 디렉토리 (File 객체)
                    // 예: /data/data/com.anam.wallet/files
                    // $ 기호 - 문자열 템플릿
                    // ex) val message1 = "이름: $name"  // "이름: 홍길동"

                    // 실제 생성되는 경로
                    // context.filesDir = /data/data/com.anam.wallet/files
                    // "$MINI_APPS_DIR/$appId" = "miniapps/com.anam.ethereum"
                    // 최종 경로: /data/data/com.anam.wallet/files/miniapps/com.anam.ethereum
                    val targetDir = File(context.filesDir, "$MINI_APPS_DIR/$appId")
                    // 조건 1: targetDir.exists() - 파일이나 디렉토리가 존재하는가?
                    // 조건 2: targetDir.isDirectory - 그것이 디렉토리(폴더)인가?
                    if (targetDir.exists() && targetDir.isDirectory) {
                        Log.d(TAG, "App $appId already extracted, skipping...")
                        return@forEachIndexed
                    }

                    // assets 폴더의 ZIP 파일을 → filesDir로 압축 해제 함수(아래)
                    extractFromAssets(zipFileName, appId)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize $zipFileName", e)
                }
            }
            
            // Mark as initialized
            prefs.edit().putBoolean(INITIALIZED_KEY, true).apply()
            Log.d(TAG, "All mini apps initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize mini apps", e)
            throw e
        }
    }
    
    /**
     * assets의 ZIP 파일을 filesDir로 추출
     */
    private fun extractFromAssets(zipFileName: String, appId: String) {
        val targetDir = File(context.filesDir, "$MINI_APPS_DIR/$appId")
        targetDir.mkdirs()
        
        context.assets.open("$MINI_APPS_DIR/$zipFileName").use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val file = File(targetDir, entry.name)
                    
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                    }
                    
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
        }
        
        Log.d(TAG, "Successfully extracted $appId to ${targetDir.absolutePath}")
    }
    
    /**
     * 초기화 상태 리셋 (디버깅용)
     */
    fun resetInitialization() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(INITIALIZED_KEY, false).apply()
        
        // Delete all extracted files
        val miniappsDir = File(context.filesDir, MINI_APPS_DIR)
        if (miniappsDir.exists()) {
            miniappsDir.deleteRecursively()
        }
        
        Log.d(TAG, "Reset initialization state")
    }
}