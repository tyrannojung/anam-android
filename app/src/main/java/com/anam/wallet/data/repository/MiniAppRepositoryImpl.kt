package com.anam.wallet.data.repository

import com.anam.wallet.domain.repository.MiniAppRepository
import com.anam.wallet.data.datasource.miniapp.MiniAppFileManager
import com.anam.wallet.data.datasource.miniapp.MiniAppLoader
import com.anam.wallet.data.datasource.miniapp.MiniAppScanner
import com.anam.wallet.model.miniapp.MiniAppManifest
import com.anam.wallet.model.miniapp.ScannedMiniApp
import com.anam.wallet.data.datasource.miniapp.MiniAppInitializer
import javax.inject.Inject

class MiniAppRepositoryImpl @Inject constructor(
    private val miniAppScanner: MiniAppScanner,
    private val miniAppLoader: MiniAppLoader,
    private val miniAppFileManager: MiniAppFileManager,
    private val miniAppInitializer: MiniAppInitializer
) : MiniAppRepository {
    
    override suspend fun getInstalledApps(): List<ScannedMiniApp> {
        return miniAppScanner.scanInstalledApps()
    }
    
    override suspend fun loadMiniApp(appId: String): MiniAppManifest? {
        return miniAppLoader.loadMiniApp(appId)
    }
    
    override suspend fun installApp(appId: String): Boolean {
        return miniAppFileManager.installFromAssets(appId)
    }
    
    override suspend fun deleteApp(appId: String): Boolean {
        return miniAppFileManager.deleteApp(appId)
    }
    
    override suspend fun validateApp(appId: String): Boolean {
        return miniAppFileManager.validateAppIntegrity(appId)
    }
    
    override fun getAppBasePath(appId: String): String {
        return miniAppFileManager.getAppBasePath(appId)
    }
    
    override fun clearCache() {
        miniAppScanner.clearCache()
    }
    
    override suspend fun initializeBuiltInApps(onProgress: (Int, Int) -> Unit) {
        miniAppInitializer.initializeBuiltInMiniApps(onProgress)
    }
}