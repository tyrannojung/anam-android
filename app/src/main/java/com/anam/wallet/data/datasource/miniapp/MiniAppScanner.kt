package com.anam.wallet.data.datasource.miniapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.anam.wallet.R
import com.anam.wallet.model.miniapp.ScannedMiniApp

class MiniAppScanner(
    private val context: Context
) {
    private val miniAppLoader = MiniAppLoader(context)
    private val fileManager = MiniAppFileManager(context)
    private val cachedApps = mutableMapOf<String, ScannedMiniApp>()
    
    suspend fun scanInstalledApps(): List<ScannedMiniApp> = withContext(Dispatchers.IO) {
        if (cachedApps.isNotEmpty()) {
            Log.d(TAG, "Returning cached apps: ${cachedApps.size}")
            return@withContext cachedApps.values.toList()
        }
        
        try {
            // FileManager를 통해 설치된 앱 목록 가져오기
            val installedAppIds = fileManager.getInstalledApps()
            Log.d(TAG, "Found ${installedAppIds.size} installed apps")
            
            val scannedApps = installedAppIds.mapNotNull { appId ->
                try {
                    Log.d(TAG, "Processing installed app: $appId")
                    
                    // MiniAppLoader를 통해 manifest 로드
                    val manifest = miniAppLoader.loadMiniApp(appId)
                    if (manifest != null) {
                        // FileManager를 통해 아이콘 로드
                        val iconBitmap = manifest.icon?.let { iconPath ->
                            fileManager.loadAppIconBitmap(appId, iconPath)
                        }
                        
                        // 타입별 기본값 설정
                        val (primaryColor, balance, fallbackIcon) = when (manifest.type) {
                            "blockchain" -> {
                                val color = getBlockchainColor(appId)
                                val balance = getBlockchainBalance(appId)
                                val icon = getBlockchainIcon(appId)
                                Triple(color, balance, icon)
                            }
                            "app" -> {
                                val color = getAppColor(appId)
                                val icon = getAppIcon(appId)
                                Triple(color, null, icon)
                            }
                            else -> Triple(Color(0xFF2196F3), null, null)
                        }
                        
                        ScannedMiniApp(
                            appId = appId,
                            name = manifest.name,
                            type = manifest.type,
                            version = manifest.version,
                            iconBitmap = iconBitmap,
                            primaryColor = primaryColor,
                            balance = balance,
                            fallbackIconRes = fallbackIcon
                        )
                    } else {
                        Log.e(TAG, "Failed to load manifest for $appId")
                        null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to scan miniapp: $appId", e)
                    null
                }
            }
            
            // 캐시에 저장
            scannedApps.forEach { app ->
                cachedApps[app.appId] = app
            }
            
            Log.d(TAG, "Successfully scanned ${scannedApps.size} miniapps")
            scannedApps
        } catch (e: Exception) {
            Log.e(TAG, "Failed to scan miniapps", e)
            emptyList()
        }
    }
    
    // 타입별 색상 (하드코딩 - 나중에 manifest에서 읽도록 개선 가능)
    private fun getBlockchainColor(appId: String): Color {
        return when (appId) {
            "com.anam.ethereum" -> Color(0xFF627EEA)
            "com.anam.bitcoin" -> Color(0xFFF7931A)
            "com.anam.solana" -> Color(0xFF00FFA3)
            else -> Color(0xFF627EEA) // 기본 블록체인 색상
        }
    }
    
    private fun getAppColor(appId: String): Color {
        return when (appId) {
            "kr.go.government24" -> Color(0xFF1976D2)
            else -> Color(0xFF2196F3) // 기본 앱 색상
        }
    }
    
    // 블록체인 잔액 (하드코딩 - 나중에 실제 API 연동)
    private fun getBlockchainBalance(appId: String): String {
        return when (appId) {
            "com.anam.ethereum" -> "1.5 ETH"
            "com.anam.bitcoin" -> "0.05 BTC"
            "com.anam.solana" -> "100 SOL"
            else -> "0"
        }
    }
    
    // 폴백 아이콘 리소스 (동적 로딩 실패 시)
    private fun getBlockchainIcon(appId: String): Int? {
        return when (appId) {
            "com.anam.ethereum" -> R.drawable.ic_blockchain_ethereum
            "com.anam.bitcoin" -> R.drawable.ic_blockchain_bitcoin
            "com.anam.solana" -> R.drawable.ic_blockchain_solana
            else -> null
        }
    }
    
    private fun getAppIcon(appId: String): Int? {
        return when (appId) {
            "kr.go.government24" -> R.drawable.ic_blockchain_gov
            else -> null
        }
    }
    
    fun clearCache() {
        cachedApps.clear()
    }
    
    companion object {
        private const val TAG = "MiniAppScanner"
    }
}