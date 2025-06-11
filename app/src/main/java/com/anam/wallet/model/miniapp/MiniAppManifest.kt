package com.anam.wallet.model.miniapp

/**
 * 미니앱 매니페스트 정보
 * manifest.json 파일의 내용을 담는 데이터 클래스
 */
data class MiniAppManifest(
    val appId: String,
    val type: String,  // "app" or "blockchain"
    val name: String,
    val version: String,
    val icon: String? = null,
    val description: String? = null,
    val pages: List<String> = emptyList(),
    val window: WindowConfig? = null,
    val permissions: List<String> = emptyList()
)

data class WindowConfig(
    val navigationBarTextStyle: String? = null,
    val navigationBarTitleText: String? = null,
    val navigationBarBackgroundColor: String? = null,
    val backgroundColor: String? = null
)