package com.anam.wallet

import android.content.Context
import android.util.Log
import com.anam.wallet.core.IPaymentModule
import com.anam.wallet.model.AccountInfo
import com.anam.wallet.model.BalanceSummary
import com.anam.wallet.model.ModuleInfo
import com.anam.wallet.model.NetworkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages payment modules including download, installation, and loading
 */
class ModuleManager(private val context: Context) {

    companion object {
        private const val TAG = "ModuleManager"
        private const val API_BASE_URL = "http://localhost:8080/modules"
        private const val DEFAULT_MODULE_ID = "3" // Default module to download
        private const val CONNECT_TIMEOUT = 15000
        private const val READ_TIMEOUT = 15000
    }

    // Module loader for DexClassLoader operations
    private val moduleLoader = ModuleLoader(context)

    // Cache for download states
    private val downloadStates = ConcurrentHashMap<String, DownloadState>()

    // Directory to store downloaded APK files
    private val modulesDir by lazy {
        File(context.filesDir, "modules").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Get list of loaded module IDs
     */
    fun getLoadedModuleIds(): Set<String> = moduleLoader.getLoadedModuleIds()

    /**
     * Get a loaded module by ID (internal use only)
     */
    internal fun getModule(moduleId: String): IPaymentModule? = moduleLoader.getModule(moduleId)

    /**
     * Check if a module is loaded
     */
    fun isModuleLoaded(moduleId: String): Boolean = moduleLoader.isModuleLoaded(moduleId)

    /**
     * Get full module information for a module
     */
    fun getFullModuleInfo(moduleId: String): ModuleInfo? = moduleLoader.getFullModuleInfo(moduleId)

    /**
     * Get accounts for a module
     */
    fun getModuleAccounts(moduleId: String): List<AccountInfo>? = moduleLoader.getModuleAccounts(moduleId)

    /**
     * Get balance summary for a module account
     */
    fun getModuleBalanceSummary(moduleId: String, address: String): BalanceSummary? =
        moduleLoader.getModuleBalanceSummary(moduleId, address)

    /**
     * Get network information for a module
     */
    fun getModuleNetworkInfo(moduleId: String): NetworkInfo? = moduleLoader.getModuleNetworkInfo(moduleId)

    /**
     * Download and load a module
     *
     * @param moduleId ID of the module to download
     * @param onProgress Callback for download progress updates
     * @param onComplete Callback for completion (success or failure)
     */
    suspend fun downloadAndLoadModule(
        moduleId: String = DEFAULT_MODULE_ID,
        onProgress: (Int) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            // 1. Check if this module is already being downloaded
            if (downloadStates.containsKey(moduleId) &&
                downloadStates[moduleId] == DownloadState.IN_PROGRESS) {
                onComplete(false, "Module is already being downloaded")
                return
            }

            // 2. Update download state
            downloadStates[moduleId] = DownloadState.IN_PROGRESS

            // 3. Download the module
            onProgress(10)
            val apkFile = downloadModule(moduleId, onProgress)

            // 4. Verify download
            if (!apkFile.exists() || apkFile.length() == 0L) {
                downloadStates[moduleId] = DownloadState.FAILED
                onComplete(false, "Downloaded file is invalid or empty")
                return
            }

            // 5. Load the module
            onProgress(90)
            val success = moduleLoader.loadModule(moduleId, apkFile)

            // 6. Update state and notify completion
            downloadStates[moduleId] = if (success) DownloadState.COMPLETED else DownloadState.FAILED

            val module = moduleLoader.getModule(moduleId)
            val message = if (success && module != null) {
                "Successfully loaded ${module.getName()} (${module.getSymbol()})"
            } else {
                "Failed to load module"
            }

            onProgress(100)
            onComplete(success, message)

        } catch (e: Exception) {
            Log.e(TAG, "Error downloading/loading module: $moduleId", e)
            downloadStates[moduleId] = DownloadState.FAILED
            onComplete(false, "Error: ${e.message}")
        }
    }

    /**
     * Download a module from the API
     *
     * @param moduleId ID of the module to download
     * @param onProgress Callback for download progress updates (0-80%)
     * @return Downloaded APK file
     */
    private suspend fun downloadModule(moduleId: String, onProgress: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        val moduleFile = File(modulesDir, "module_${moduleId}.apk")
        val url = URL("$API_BASE_URL/$moduleId/download")

        // Setup connection
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECT_TIMEOUT
        connection.readTimeout = READ_TIMEOUT

        try {
            connection.connect()

            // Check response code
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server returned error: ${connection.responseCode} ${connection.responseMessage}")
            }

            // Get file size if available
            val fileSize = connection.contentLength

            // Create output file
            val outputStream = FileOutputStream(moduleFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalRead = 0

            // Read data
            connection.inputStream.use { input ->
                outputStream.use { output ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        // Update progress (scale to 10-80%)
                        if (fileSize > 0) {
                            val progress = 10 + (totalRead.toFloat() / fileSize * 70).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            Log.d(TAG, "Module downloaded successfully: $moduleId to ${moduleFile.absolutePath}")
            moduleFile

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Unload a module
     */
    fun unloadModule(moduleId: String): Boolean {
        return moduleLoader.unloadModule(moduleId)
    }

    /**
     * Check download status of a module
     */
    fun getDownloadState(moduleId: String): DownloadState {
        return downloadStates[moduleId] ?: DownloadState.NOT_STARTED
    }
}

/**
 * Download state for modules
 */
enum class DownloadState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}