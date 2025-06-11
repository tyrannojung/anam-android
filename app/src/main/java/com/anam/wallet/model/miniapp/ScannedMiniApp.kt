package com.anam.wallet.model.miniapp

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

/**
 * 스캔된 미니앱 정보
 * MiniAppScanner에서 filesDir을 스캔한 결과
 */
data class ScannedMiniApp(
    val appId: String,
    val name: String,
    val type: String,  // "blockchain" or "app"
    val version: String,
    val iconBitmap: Bitmap? = null,
    val primaryColor: Color,
    val balance: String? = null,  // 블록체인 타입만
    // 임시 하드코딩 아이콘 (동적 로딩 실패 시 폴백)
    val fallbackIconRes: Int? = null
)