package com.anam.wallet.miniapp

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.anam.wallet.model.miniapp.MiniAppManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * 미니앱 파일 관리를 담당하는 클래스
 * - 설치, 검증, 로드, 삭제 등 파일 관련 모든 작업 처리
 */
class MiniAppFileManager(private val context: Context) {
    
    companion object {
        private const val TAG = "MiniAppFileManager"
        private const val MINI_APPS_DIR = "miniapps"
        private const val MANIFEST_FILE = "manifest.json"
        private const val PREFS_NAME = "mini_app_prefs"
        private const val INITIALIZED_KEY = "mini_apps_initialized"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 모든 내장 미니앱을 assets에서 설치
     */
    suspend fun installAllFromAssets(onProgress: (Int, Int) -> Unit = { _, _ -> }): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 이미 초기화되었는지 확인
                if (isInitialized()) {
                    Log.d(TAG, "Mini apps already initialized")
                    return@withContext true
                }
                
                val assetManager = context.assets
                val miniappFiles = assetManager.list("miniapps") ?: emptyArray()
                val zipFiles = miniappFiles.filter { it.endsWith(".zip") }
                
                Log.d(TAG, "Found ${zipFiles.size} mini apps to install")
                
                zipFiles.forEachIndexed { index, zipFile ->
                    val appId = zipFile.substringBefore("_")
                    Log.d(TAG, "Installing mini app: $appId from $zipFile")
                    
                    val success = installFromAssetFile("miniapps/$zipFile", appId)
                    if (!success) {
                        Log.e(TAG, "Failed to install: $appId")
                        return@withContext false
                    }
                    
                    onProgress(index + 1, zipFiles.size)
                }
                
                // 초기화 완료 표시
                markAsInitialized()
                Log.d(TAG, "All mini apps installed successfully")
                true
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to install mini apps", e)
                false
            }
        }
    }
    
    /**
     * 특정 미니앱을 assets에서 설치/복구
     */
    suspend fun installFromAssets(appId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val assetPath = findAssetPath(appId)
                if (assetPath == null) {
                    Log.e(TAG, "No asset found for appId: $appId")
                    return@withContext false
                }
                
                installFromAssetFile(assetPath, appId)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to install app: $appId", e)
                false
            }
        }
    }
    
    /**
     * 미니앱이 올바르게 설치되었는지 확인
     */
    fun isAppInstalled(appId: String): Boolean {
        val appDir = getAppDirectory(appId)
        return appDir.exists() && File(appDir, MANIFEST_FILE).exists()
    }
    
    /**
     * 미니앱의 무결성 검증
     */
    fun validateAppIntegrity(appId: String): Boolean {
        val appDir = getAppDirectory(appId)
        
        // 기본 구조 확인
        if (!appDir.exists() || !appDir.isDirectory) {
            return false
        }
        
        // manifest.json 확인
        val manifestFile = File(appDir, MANIFEST_FILE)
        if (!manifestFile.exists() || !manifestFile.isFile) {
            // 레거시 구조 확인 (subdirectory)
            val subDirs = appDir.listFiles { file: File -> file.isDirectory }
            if (subDirs != null && subDirs.isNotEmpty()) {
                val subManifest = File(subDirs[0], MANIFEST_FILE)
                if (!subManifest.exists()) {
                    return false
                }
            } else {
                return false
            }
        }
        
        // 필수 파일들 확인 (app.js, app.css 등)
        // 추후 manifest에 정의된 파일들도 검증 가능
        
        return true
    }
    
    /**
     * 미니앱 디렉토리 반환
     */
    fun getAppDirectory(appId: String): File {
        return File(context.filesDir, "$MINI_APPS_DIR/$appId")
    }
    
    /**
     * 미니앱 매니페스트 로드
     */
    fun loadManifest(appId: String): MiniAppManifest? {
        try {
            val appDir = getAppDirectory(appId)
            var manifestFile = File(appDir, MANIFEST_FILE)
            
            // 레거시 구조 확인 (subdirectory)
            if (!manifestFile.exists()) {
                val subDirs = appDir.listFiles { file: File -> file.isDirectory }
                if (subDirs != null && subDirs.isNotEmpty()) {
                    manifestFile = File(subDirs[0], MANIFEST_FILE)
                }
            }
            
            if (!manifestFile.exists()) {
                Log.e(TAG, "Manifest not found for: $appId")
                return null
            }
            
            val manifestJson = manifestFile.readText()
            return parseManifest(manifestJson)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load manifest for: $appId", e)
            return null
        }
    }
    
    /**
     * 미니앱의 기본 경로 반환 (WebView 로드용)
     */
    fun getAppBasePath(appId: String): String {
        val appDir = getAppDirectory(appId)
        
        // 레거시 구조 확인
        val manifestFile = File(appDir, MANIFEST_FILE)
        if (!manifestFile.exists()) {
            val subDirs = appDir.listFiles { file: File -> file.isDirectory }
            if (subDirs != null && subDirs.isNotEmpty()) {
                return "file://${subDirs[0].absolutePath}/"
            }
        }
        
        return "file://${appDir.absolutePath}/"
    }
    
    /**
     * 미니앱 아이콘 경로 반환
     */
    fun getAppIconPath(appId: String, iconPath: String?): String? {
        if (iconPath == null) return null
        
        val appDir = getAppDirectory(appId)
        
        // 직접 경로 확인
        var iconFile = File(appDir, iconPath)
        
        // 레거시 구조 확인
        if (!iconFile.exists()) {
            val subDirs = appDir.listFiles { file: File -> file.isDirectory }
            if (subDirs != null && subDirs.isNotEmpty()) {
                iconFile = File(subDirs[0], iconPath)
            }
        }
        
        return if (iconFile.exists()) iconFile.absolutePath else null
    }
    
    /**
     * 미니앱 아이콘 비트맵 로드
     */
    fun loadAppIconBitmap(appId: String, iconPath: String?): android.graphics.Bitmap? {
        val path = getAppIconPath(appId, iconPath) ?: return null
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load icon bitmap: $path", e)
            null
        }
    }
    
    /**
     * 미니앱 삭제
     */
    fun deleteApp(appId: String): Boolean {
        return try {
            val appDir = getAppDirectory(appId)
            if (appDir.exists()) {
                appDir.deleteRecursively()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete app: $appId", e)
            false
        }
    }
    
    /**
     * 설치된 모든 미니앱 목록 반환
     */
    fun getInstalledApps(): List<String> {
        val miniappsDir = File(context.filesDir, MINI_APPS_DIR)
        if (!miniappsDir.exists()) return emptyList()
        
        return miniappsDir.listFiles { file: File -> file.isDirectory }
            ?.map { it.name }
            ?: emptyList()
    }
    
    // Private helper methods
    
    private fun isInitialized(): Boolean {
        return prefs.getBoolean(INITIALIZED_KEY, false)
    }
    
    private fun markAsInitialized() {
        prefs.edit().putBoolean(INITIALIZED_KEY, true).apply()
    }
    
    private fun findAssetPath(appId: String): String? {
        return try {
            val miniappFiles = context.assets.list("miniapps") ?: emptyArray()
            miniappFiles.find { it.startsWith("${appId}_") && it.endsWith(".zip") }
                ?.let { "miniapps/$it" }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find asset for: $appId", e)
            null
        }
    }
    
    private suspend fun installFromAssetFile(assetPath: String, appId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val targetDir = getAppDirectory(appId)
                
                // 기존 디렉토리 삭제
                if (targetDir.exists()) {
                    targetDir.deleteRecursively()
                }
                targetDir.mkdirs()
                
                // ZIP 파일 추출
                context.assets.open(assetPath).use { inputStream ->
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
                
                Log.d(TAG, "Successfully installed: $appId to ${targetDir.absolutePath}")
                true
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to install from asset: $assetPath", e)
                false
            }
        }
    }
    
    private fun parseManifest(json: String): MiniAppManifest {
        val jsonObject = JSONObject(json)
        
        // Parse pages array
        val pages = if (jsonObject.has("pages")) {
            val pagesArray = jsonObject.getJSONArray("pages")
            (0 until pagesArray.length()).map { pagesArray.getString(it) }
        } else {
            emptyList()
        }
        
        // window과 permissions는 제거됨 (W3C MiniApp 표준에 따라 간소화)
        
        return MiniAppManifest(
            appId = jsonObject.getString("app_id"),
            type = jsonObject.optString("type", "app"),
            name = jsonObject.getString("name"),
            version = jsonObject.getString("version"),
            icon = jsonObject.optString("icon"),
            description = jsonObject.optString("description"),
            pages = pages
        )
    }
}