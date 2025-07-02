package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject
import com.anam.wallet.model.miniapp.MiniAppManifest

class MiniAppJavaScriptBridge(
    private val context: Context,
    private val manifest: MiniAppManifest,
    private val onPaymentRequest: ((JSONObject, String) -> Unit)? = null,
    private val onPaymentResponse: ((String, JSONObject) -> Unit)? = null,
    private val onVPRequest: ((JSONObject) -> Unit)? = null,
    private val webView: WebView? = null
) {
    companion object {
        private const val TAG = "MiniAppJSBridge"
    }
    
    @JavascriptInterface
    fun requestPayment(paymentDataJson: String) {
        Log.d(TAG, "requestPayment called with: $paymentDataJson")
        Log.d(TAG, "onPaymentRequest handler is null: ${onPaymentRequest == null}")
        
        try {
            val paymentData = JSONObject(paymentDataJson)
            
            // 블록체인 WebView로 결제 요청 전달
            Log.d(TAG, "Before runOnUiThread")
            context.runOnUiThread {
                Log.d(TAG, "Inside runOnUiThread")
                if (onPaymentRequest != null) {
                    Log.d(TAG, "Invoking payment request handler")
                    onPaymentRequest.invoke(paymentData, "")
                    Log.d(TAG, "Payment request sent to handler")
                } else {
                    Log.e(TAG, "No payment request handler registered!")
                }
            }
            Log.d(TAG, "After runOnUiThread")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process payment request", e)
        }
    }
    
    @JavascriptInterface
    fun sendPaymentResponse(requestId: String, responseDataJson: String) {
        Log.d(TAG, "sendPaymentResponse: requestId=$requestId, response=$responseDataJson")
        
        try {
            val responseData = JSONObject(responseDataJson)
            
            // MiniAppManager로 응답 전달
            context.runOnUiThread {
                onPaymentResponse?.invoke(requestId, responseData)
                Log.d(TAG, "Payment response sent to handler")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send payment response", e)
        }
    }
    
    @JavascriptInterface
    fun requestVP(vpRequestJson: String) {
        Log.d(TAG, "requestVP called with: $vpRequestJson")
        
        try {
            val vpRequest = JSONObject(vpRequestJson)
            
            context.runOnUiThread {
                if (onVPRequest != null) {
                    Log.d(TAG, "Invoking VP request handler")
                    onVPRequest.invoke(vpRequest)
                } else {
                    Log.e(TAG, "No VP request handler registered!")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process VP request", e)
        }
    }
    
    /**
     * 페이지 이동 API
     * 
     * JavaScript에서 호출:
     * - window.anam.navigateTo('pages/payment/payment')
     * - window.anam.navigateTo('pages/success/success?txHash=0x123')
     * 
     * 보안 검증:
     * 1. manifest의 pages 배열에 포함된 페이지인지 확인
     * 2. 경로 탐색 공격 방지 (../ 패턴 차단)
     * 3. 허용된 페이지만 이동 가능
     */
    @JavascriptInterface
    fun navigateTo(path: String) {
        Log.d(TAG, "navigateTo called with path: $path")
        
        context.runOnUiThread {
            try {
                // 경로 탐색 공격 방지
                if (path.contains("../") || path.contains("..\\")) {
                    Log.e(TAG, "Path traversal attack detected: $path")
                    showError("잘못된 경로입니다")
                    return@runOnUiThread
                }
                
                // 쿼리 파라미터 분리
                val pathWithoutQuery = path.substringBefore("?")
                val queryString = if (path.contains("?")) path.substringAfter("?") else ""
                
                // .html 확장자 제거 (manifest에는 확장자 없이 저장됨)
                val pagePath = pathWithoutQuery.removeSuffix(".html")
                
                // manifest의 pages 배열에 포함되어 있는지 확인
                if (!manifest.pages.contains(pagePath)) {
                    Log.e(TAG, "Page not allowed in manifest: $pagePath")
                    Log.d(TAG, "Allowed pages: ${manifest.pages}")
                    showError("허용되지 않은 페이지입니다: $pagePath")
                    return@runOnUiThread
                }
                
                // WebView가 있는지 확인
                webView?.let { wv ->
                    // 커스텀 스킴 URL로 변환
                    val url = buildUrl(pagePath, queryString)
                    Log.d(TAG, "Navigating to: $url")
                    wv.loadUrl(url)
                } ?: Log.e(TAG, "WebView not available for navigation")
                
            } catch (e: Exception) {
                Log.e(TAG, "Navigation error", e)
                showError("페이지 이동 중 오류가 발생했습니다")
            }
        }
    }
    
    /**
     * WebViewAssetLoader URL 생성
     */
    private fun buildUrl(pagePath: String, queryString: String): String {
        val baseUrl = AssetLoaderWebViewClient.getBaseUrlForApp(manifest.appId)
        val fullUrl = "$baseUrl$pagePath.html"
        return if (queryString.isNotEmpty()) {
            "$fullUrl?$queryString"
        } else {
            fullUrl
        }
    }
    
    /**
     * JavaScript에 에러 메시지 표시
     */
    private fun showError(message: String) {
        webView?.evaluateJavascript(
            "alert('$message');",
            null
        )
    }
    
    
    private fun Context.runOnUiThread(action: () -> Unit) {
        Log.d(TAG, "runOnUiThread called, context is Activity: ${this is android.app.Activity}")
        if (this is android.app.Activity) {
            runOnUiThread(action)
        } else {
            Log.e(TAG, "Context is not an Activity, trying Handler")
            // Activity가 아닌 경우 Handler 사용
            android.os.Handler(android.os.Looper.getMainLooper()).post(action)
        }
    }
}