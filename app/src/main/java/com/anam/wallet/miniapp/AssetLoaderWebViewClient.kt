package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import com.anam.wallet.model.miniapp.MiniAppManifest
import java.io.File

/**
 * WebViewAssetLoader를 사용하여 미니앱 리소스를 로드하는 WebViewClient
 * 
 * 기존 커스텀 스킴(anam://)의 origin 격리 문제를 해결하기 위해
 * Google이 권장하는 WebViewAssetLoader를 사용합니다.
 * 
 * URL 형식: https://{appId}.miniapp.local/path/to/resource
 * 
 * @param appId 미니앱의 고유 ID (예: com.anam.ethereum)
 * @param basePath 미니앱 파일들이 저장된 기본 경로
 * @param manifest 미니앱의 manifest 정보 (페이지 화이트리스트 포함)
 * @param onPageFinishedCallback 페이지 로드 완료 시 호출될 콜백
 */
class AssetLoaderWebViewClient(
    private val appId: String,
    private val basePath: String,
    private val manifest: MiniAppManifest? = null,
    private val onPageFinishedCallback: ((WebView) -> Unit)? = null
) : WebViewClient() {
    
    companion object {
        private const val TAG = "AssetLoaderWebView"
        const val DOMAIN_SUFFIX = ".miniapp.local"
        
        /**
         * 앱 ID로부터 도메인 생성
         * 예: com.anam.ethereum → com.anam.ethereum.miniapp.local
         */
        fun getDomainForApp(appId: String): String {
            return "$appId$DOMAIN_SUFFIX"
        }
        
        /**
         * 앱 ID로부터 기본 URL 생성
         * 예: com.anam.ethereum → https://com.anam.ethereum.miniapp.local/
         */
        fun getBaseUrlForApp(appId: String): String {
            return "https://${getDomainForApp(appId)}/"
        }
    }
    
    // WebViewAssetLoader 인스턴스
    private val assetLoader: WebViewAssetLoader
    
    init {
        // LocalPathHandler: 로컬 파일 시스템의 파일을 제공
        val pathHandler = WebViewAssetLoader.PathHandler { path ->
            Log.d(TAG, "Loading resource: $path")
            
            // HTML 파일인 경우 화이트리스트 검증
            if (path.endsWith(".html") && !isPageAllowed(path)) {
                Log.e(TAG, "Page not allowed in manifest: $path")
                return@PathHandler createErrorResponse(
                    403,
                    "Forbidden",
                    "페이지가 허용되지 않았습니다: $path"
                )
            }
            
            // 실제 파일 경로 생성
            val fullPath = if (basePath.endsWith("/")) {
                basePath + path
            } else {
                "$basePath/$path"
            }
            
            val file = File(fullPath)
            if (!file.exists()) {
                Log.w(TAG, "File not found: $fullPath")
                return@PathHandler null
            }
            
            // MIME 타입 결정
            val extension = file.extension.lowercase()
            val mimeType = when (extension) {
                "html" -> "text/html"
                "js" -> "application/javascript"
                "css" -> "text/css"
                "json" -> "application/json"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "svg" -> "image/svg+xml"
                else -> "application/octet-stream"
            }
            
            WebResourceResponse(
                mimeType,
                "UTF-8",
                file.inputStream()
            )
        }
        
        // WebViewAssetLoader 설정
        assetLoader = WebViewAssetLoader.Builder()
            .setDomain(getDomainForApp(appId))  // 앱별 고유 도메인
            .addPathHandler("/", pathHandler)     // 모든 경로에 대해 pathHandler 사용
            .build()
    }
    
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        // WebViewAssetLoader가 자동으로 처리
        // https://{appId}.miniapp.local/* 형식의 URL만 가로채서 처리
        return assetLoader.shouldInterceptRequest(request?.url)
            ?: super.shouldInterceptRequest(view, request)
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d(TAG, "Page loaded: $url")
        
        // Origin 확인을 위한 로그
        view?.evaluateJavascript("window.location.origin") { origin ->
            Log.d(TAG, "Page origin: $origin")
        }
        
        view?.let { onPageFinishedCallback?.invoke(it) }
    }
    
    /**
     * 페이지가 manifest의 pages 배열에 포함되어 있는지 확인
     */
    private fun isPageAllowed(resourcePath: String): Boolean {
        // manifest가 없으면 모든 페이지 허용 (블록체인 미니앱)
        if (manifest == null) {
            Log.d(TAG, "No manifest - allowing all pages (blockchain)")
            return true
        }
        
        // .html 확장자 제거
        val pagePath = resourcePath.removeSuffix(".html")
        
        // manifest의 pages 배열에 포함되어 있는지 확인
        val isAllowed = manifest.pages?.contains(pagePath) ?: false
        
        Log.d(TAG, "Page validation: $pagePath -> ${if (isAllowed) "ALLOWED" else "BLOCKED"}")
        
        return isAllowed
    }
    
    /**
     * 에러 응답 생성
     */
    private fun createErrorResponse(
        code: Int,
        reason: String,
        message: String
    ): WebResourceResponse {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Error $code</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px;
                        background-color: #f5f5f5;
                    }
                    h1 { color: #e74c3c; }
                    p { color: #555; margin-top: 20px; }
                </style>
            </head>
            <body>
                <h1>Error $code: $reason</h1>
                <p>$message</p>
            </body>
            </html>
        """.trimIndent()
        
        return WebResourceResponse(
            "text/html",
            "UTF-8",
            code,
            reason,
            mapOf("Content-Type" to "text/html; charset=UTF-8"),
            html.byteInputStream()
        )
    }
}