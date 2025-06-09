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
    
    companion object {
        private const val TAG = "MiniAppInitializer"
        private const val MINI_APPS_DIR = "miniapps"
        private const val INITIALIZED_KEY = "miniapps_initialized"
        private const val PREFS_NAME = "miniapp_prefs"
    }
    
    /**
     * 내장 미니앱들을 초기화 (assets → filesDir)
     */
    suspend fun initializeBuiltInMiniApps(
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isInitialized = prefs.getBoolean(INITIALIZED_KEY, false)
        
        if (isInitialized) {
            Log.d(TAG, "Mini apps already initialized, skipping...")
            return@withContext
        }
        
        try {
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
                    val targetDir = File(context.filesDir, "$MINI_APPS_DIR/$appId")
                    if (targetDir.exists() && targetDir.isDirectory) {
                        Log.d(TAG, "App $appId already extracted, skipping...")
                        return@forEachIndexed
                    }
                    
                    // Extract ZIP from assets to filesDir
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