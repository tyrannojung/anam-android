package com.anam.wallet.domain.repository

import com.anam.wallet.model.miniapp.MiniAppManifest
import com.anam.wallet.model.miniapp.ScannedMiniApp

interface MiniAppRepository {
    suspend fun getInstalledApps(): List<ScannedMiniApp>
    suspend fun loadMiniApp(appId: String): MiniAppManifest?
    suspend fun installApp(appId: String): Boolean
    suspend fun deleteApp(appId: String): Boolean
    suspend fun validateApp(appId: String): Boolean
    fun getAppBasePath(appId: String): String
    fun clearCache()
    suspend fun initializeBuiltInApps(onProgress: (Int, Int) -> Unit)
}