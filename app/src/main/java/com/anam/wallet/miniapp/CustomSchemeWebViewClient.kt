package com.anam.wallet.miniapp

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.anam.wallet.model.miniapp.MiniAppManifest
import java.io.File
import java.io.FileInputStream

/**
 * 커스텀 스킴(anam://)을 처리하는 WebViewClient
 * 
 * 기존 file:// 프로토콜의 보안 문제를 해결하기 위해 만든 클래스입니다.
 * anam://miniapp-{appId}/path/to/resource 형식의 URL을 가로채서
 * 실제 파일 시스템의 파일로 매핑합니다.
 * 
 * @param appId 미니앱의 고유 ID (예: kr.go.government24)
 * @param basePath 미니앱 파일들이 저장된 기본 경로
 * @param manifest 미니앱의 manifest 정보 (페이지 화이트리스트 포함)
 * @param onPageFinishedCallback 페이지 로드 완료 시 호출될 콜백
 */
class CustomSchemeWebViewClient(
    private val appId: String,
    var basePath: String,
    var manifest: MiniAppManifest?,
    private val onPageFinishedCallback: ((WebView) -> Unit)? = null
) : WebViewClient() {
    
    companion object {
        private const val TAG = "CustomScheme"
        private const val SCHEME = "anam"  // 우리가 사용할 커스텀 스킴
        
        // 파일 확장자별 MIME 타입 매핑
        // WebView가 파일을 올바르게 해석하려면 적절한 MIME 타입이 필요합니다
        private val MIME_TYPES = mapOf(
            "html" to "text/html",
            "js" to "application/javascript",
            "css" to "text/css",
            "json" to "application/json",
            "png" to "image/png",
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "svg" to "image/svg+xml"
        )
    }
    
    /**
     * WebView가 리소스를 요청할 때 호출되는 메서드
     * 
     * 이 메서드는 WebView가 어떤 URL로든 요청을 보낼 때마다 호출됩니다.
     * 우리는 여기서 anam:// 스킴을 가진 URL만 가로채서 처리합니다.
     * 
     * 예시:
     * - anam://miniapp-kr.go.government24/pages/index/index.html
     * - anam://miniapp-kr.go.government24/app.css
     * - anam://miniapp-kr.go.government24/assets/images/logo.png
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        
        Log.d(TAG, "shouldInterceptRequest called for: $url")
        
        // anam:// 으로 시작하는 URL만 처리
        // 다른 URL(http://, https://, file:// 등)은 기본 처리기로 넘김
        if (!url.startsWith("$SCHEME://")) {
            Log.d(TAG, "Not our scheme, passing through: $url")
            return super.shouldInterceptRequest(view, request)
        }
        
        return try {
            // 커스텀 스킴 URL을 실제 파일로 변환
            handleCustomScheme(url)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading resource: $url", e)
            null
        }
    }
    
    /**
     * 커스텀 스킴 URL을 처리하는 핵심 메서드
     * 
     * 처리 과정:
     * 1. URL 파싱: anam://miniapp-kr.go.government24/pages/index/index.html
     * 2. 리소스 경로 추출: pages/index/index.html
     * 3. 페이지 화이트리스트 검증 (HTML 파일의 경우)
     * 4. 실제 파일 경로 생성: /data/data/.../files/miniapps/kr.go.government24/pages/index/index.html
     * 5. 파일 읽기 및 WebResourceResponse로 반환
     */
    private fun handleCustomScheme(url: String): WebResourceResponse? {
        // URL 형식 검증: anam://miniapp-{appId}/path/to/resource
        val prefix = "$SCHEME://miniapp-$appId/"
        if (!url.startsWith(prefix)) {
            Log.w(TAG, "Invalid URL format: $url")
            return null
        }
        
        // URL에서 쿼리 파라미터 분리
        val fullPath = url.substring(prefix.length)
        val resourcePath = fullPath.substringBefore("?")  // 쿼리 파라미터 제거
        Log.d(TAG, "Loading resource: $resourcePath")
        
        // HTML 페이지인 경우 화이트리스트 검증
        if (resourcePath.endsWith(".html")) {
            if (!isPageAllowed(resourcePath)) {
                Log.e(TAG, "Page not allowed in manifest: $resourcePath")
                return createErrorResponse(
                    403,
                    "Forbidden",
                    "페이지가 허용되지 않았습니다: $resourcePath"
                )
            }
        }
        
        // 전체 파일 경로 생성
        val filePath = if (basePath.endsWith("/")) {
            basePath + resourcePath
        } else {
            "$basePath/$resourcePath"
        }
        
        // file:// 접두사 제거 (basePath에 포함되어 있을 수 있음)
        val cleanPath = filePath.replace("file://", "")
        val file = File(cleanPath)
        
        // 파일 존재 여부 확인
        if (!file.exists()) {
            Log.w(TAG, "File not found: $cleanPath")
            return null
        }
        
        // 파일 확장자로 MIME 타입 결정
        // 예: .html → text/html, .js → application/javascript
        val extension = file.extension.lowercase()
        val mimeType = MIME_TYPES[extension] ?: "application/octet-stream"
        
        // 파일 내용을 WebResourceResponse로 반환
        // WebView는 이 응답을 받아서 마치 네트워크에서 받은 것처럼 처리합니다
        return WebResourceResponse(
            mimeType,
            "UTF-8",
            FileInputStream(file)
        )
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d(TAG, "Page loaded: $url")
        Log.d(TAG, "Page origin: ${view?.url}")
        view?.let { onPageFinishedCallback?.invoke(it) }
    }
    
    /**
     * 페이지가 manifest의 pages 배열에 포함되어 있는지 확인
     * 
     * 예시:
     * - manifest.pages = ["pages/index/index", "pages/payment/payment"]
     * - resourcePath = "pages/index/index.html" → true
     * - resourcePath = "pages/hack/hack.html" → false
     * 
     * 블록체인 미니앱의 경우 manifest가 null일 수 있으므로, 이 경우 모든 페이지 허용
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
        val isAllowed = manifest?.pages?.contains(pagePath) ?: false
        
        Log.d(TAG, "Page validation: $pagePath -> ${if (isAllowed) "ALLOWED" else "BLOCKED"}")
        Log.d(TAG, "Allowed pages: ${manifest?.pages}")
        
        return isAllowed
    }
    
    /**
     * 에러 응답 생성
     */
    private fun createErrorResponse(code: Int, reason: String, message: String): WebResourceResponse {
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