package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

data class MiniAppManifest(
    val appId: String,
    val name: String,
    val version: String,
    val description: String? = null,
    val pages: List<String>,
    val window: WindowConfig? = null,
    val permissions: List<String> = emptyList()
)

data class WindowConfig(
    val navigationBarTextStyle: String? = null,
    val navigationBarTitleText: String? = null,
    val navigationBarBackgroundColor: String? = null,
    val backgroundColor: String? = null
)

class MiniAppLoader(private val context: Context) {
    companion object {
        private const val TAG = "MiniAppLoader"
        private const val MINI_APPS_DIR = "miniapps"
        private const val MANIFEST_FILE = "manifest.json"
    }

    fun loadMiniApp(appId: String): MiniAppManifest? {
        return try {
            val miniAppDir = extractMiniApp(appId)
            if (miniAppDir != null) {
                loadManifest(miniAppDir)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load mini app: $appId", e)
            null
        }
    }

    private fun extractMiniApp(appId: String): File? {
        try {
            val assetManager = context.assets
            
            // Check for versioned ZIP file first, then fallback to simple name
            val zipFileName = when (appId) {
                "government24" -> "apps/${appId}_v1.0.0.zip"
                "ethereum" -> "blockchains/$appId.zip"
                else -> "apps/${appId}_v1.0.0.zip"
            }
            
            val miniAppDir = File(context.filesDir, "$MINI_APPS_DIR/$appId")
            
            // Check if already extracted
            if (miniAppDir.exists() && File(miniAppDir, MANIFEST_FILE).exists()) {
                return miniAppDir
            }
            
            // Extract ZIP file
            miniAppDir.mkdirs()
            
            assetManager.open(zipFileName).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {
                        val file = File(miniAppDir, entry.name)
                        
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
            
            return miniAppDir
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract mini app: $appId", e)
            return null
        }
    }

    private fun loadManifest(miniAppDir: File): MiniAppManifest? {
        return try {
            val manifestFile = File(miniAppDir, MANIFEST_FILE)
            if (!manifestFile.exists()) {
                Log.e(TAG, "Manifest file not found: ${manifestFile.absolutePath}")
                return null
            }
            
            val manifestJson = manifestFile.readText()
            val json = JSONObject(manifestJson)
            
            // Parse pages array
            val pagesArray = json.getJSONArray("pages")
            val pages = mutableListOf<String>()
            for (i in 0 until pagesArray.length()) {
                pages.add(pagesArray.getString(i))
            }
            
            // Parse window config if exists
            val windowConfig = if (json.has("window")) {
                val windowJson = json.getJSONObject("window")
                WindowConfig(
                    navigationBarTextStyle = windowJson.optString("navigationBarTextStyle"),
                    navigationBarTitleText = windowJson.optString("navigationBarTitleText"),
                    navigationBarBackgroundColor = windowJson.optString("navigationBarBackgroundColor"),
                    backgroundColor = windowJson.optString("backgroundColor")
                )
            } else null
            
            // Parse permissions if exists
            val permissions = if (json.has("permissions")) {
                val permissionsArray = json.getJSONArray("permissions")
                val permissionsList = mutableListOf<String>()
                for (i in 0 until permissionsArray.length()) {
                    permissionsList.add(permissionsArray.getString(i))
                }
                permissionsList
            } else emptyList()
            
            MiniAppManifest(
                appId = json.getString("app_id"),
                name = json.getString("name"),
                version = json.getString("version"),
                description = json.optString("description"),
                pages = pages,
                window = windowConfig,
                permissions = permissions
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load manifest", e)
            null
        }
    }
    
    fun getMiniAppBasePath(appId: String): String {
        return "file://${context.filesDir.absolutePath}/$MINI_APPS_DIR/$appId/"
    }
}