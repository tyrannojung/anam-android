package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.io.File

/**
 * 미니앱 관리자 - WebView Pool 관리 및 미니앱 간 통신 담당
 */
class MiniAppManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "MiniAppManager"
        
        @Volatile
        private var INSTANCE: MiniAppManager? = null
        
        fun getInstance(context: Context): MiniAppManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MiniAppManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // 활성 블록체인 WebView (백그라운드에서 유지)
    private var activeBlockchainWebView: WebView? = null
    private var activeBlockchainId: String? = null
    
    // 활성 앱 WebView (포그라운드에서만 유지)
    private var activeAppWebView: WebView? = null
    private var activeAppId: String? = null
    
    // 미니앱 로더
    private val miniAppLoader = MiniAppLoader(context)
    
    // 상태 관리
    private val _activeBlockchain = MutableStateFlow<String?>(null)
    val activeBlockchain: StateFlow<String?> = _activeBlockchain
    
    /**
     * 블록체인 미니앱 활성화
     */
    suspend fun activateBlockchain(blockchainId: String): WebView? {
        Log.d(TAG, "Activating blockchain: $blockchainId")
        
        // 기존 블록체인 WebView 정리
        activeBlockchainWebView?.let {
            Log.d(TAG, "Destroying previous blockchain WebView: $activeBlockchainId")
            it.destroy()
        }
        
        // 새 블록체인 WebView 생성
        val manifest = miniAppLoader.loadMiniApp(blockchainId)
        if (manifest != null) {
            activeBlockchainWebView = createWebView(manifest).apply {
                // 백그라운드에서도 실행 가능하도록 설정
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                
                // JavaScript Bridge 추가
                val bridge = MiniAppJavaScriptBridge(
                    context = context,
                    manifest = manifest,
                    onPaymentRequest = null, // 블록체인은 결제 요청을 받는 쪽
                    onPaymentResponse = { requestId, responseData ->
                        // 블록체인에서 응답이 오면 앱으로 전달
                        sendPaymentResponse(requestId, responseData)
                    }
                )
                addJavascriptInterface(bridge, "anam")
                
                // 메인 페이지 로드
                val basePath = File(context.filesDir, "miniapps/$blockchainId").toURI().toString()
                val firstPage = manifest.pages.firstOrNull() ?: "pages/index/index"
                loadUrl("$basePath${firstPage}.html")
            }
            
            activeBlockchainId = blockchainId
            _activeBlockchain.value = blockchainId
            
            Log.d(TAG, "Blockchain activated: $blockchainId")
            return activeBlockchainWebView
        }
        
        return null
    }
    
    /**
     * 앱 미니앱 활성화
     */
    suspend fun activateApp(appId: String): WebView? {
        Log.d(TAG, "Activating app: $appId")
        
        // 기존 앱 WebView 정리
        activeAppWebView?.let {
            Log.d(TAG, "Destroying previous app WebView: $activeAppId")
            it.destroy()
        }
        
        // 새 앱 WebView 생성
        val manifest = miniAppLoader.loadMiniApp(appId)
        if (manifest != null) {
            activeAppWebView = createWebView(manifest).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                }
                
                // JavaScript Bridge 추가 - 결제 요청 콜백 포함
                val bridge = MiniAppJavaScriptBridge(
                    context = context,
                    manifest = manifest,
                    onPaymentRequest = { paymentData, _ ->
                        // 앱에서 결제 요청 시 블록체인으로 전달
                        sendPaymentToBlockchain(paymentData, this)
                    },
                    onPaymentResponse = null // 앱은 응답을 받는 쪽이므로 null
                )
                addJavascriptInterface(bridge, "anam")
                
                // 메인 페이지 로드
                val basePath = File(context.filesDir, "miniapps/$appId").toURI().toString()
                val firstPage = manifest.pages.firstOrNull() ?: "pages/index/index"
                loadUrl("$basePath${firstPage}.html")
            }
            
            activeAppId = appId
            
            Log.d(TAG, "App activated: $appId")
            return activeAppWebView
        }
        
        return null
    }
    
    /**
     * 결제 요청을 블록체인으로 전달
     */
    private fun sendPaymentToBlockchain(paymentData: JSONObject, appWebView: WebView) {
        Log.d(TAG, "sendPaymentToBlockchain called with data: $paymentData")
        Log.d(TAG, "Active blockchain WebView exists: ${activeBlockchainWebView != null}")
        Log.d(TAG, "Active blockchain ID: $activeBlockchainId")
        
        activeBlockchainWebView?.let { blockchainWebView ->
            // 요청 ID 생성 (응답 매칭용)
            val requestId = "req_${System.currentTimeMillis()}"
            paymentData.put("requestId", requestId)
            
            // 앱 WebView 저장 (응답 전달용)
            pendingRequests[requestId] = appWebView
            
            Log.d(TAG, "Dispatching payment event to blockchain WebView")
            
            // JavaScript 이벤트 발생
            val script = """
                (function() {
                    console.log('Dispatching payment event in blockchain WebView');
                    const event = new CustomEvent('paymentRequest', {
                        detail: ${paymentData.toString()}
                    });
                    window.dispatchEvent(event);
                    return 'Event dispatched';
                })();
            """.trimIndent()
            
            blockchainWebView.evaluateJavascript(script) { result ->
                Log.d(TAG, "Payment event dispatch result: $result")
            }
        } ?: Log.e(TAG, "No active blockchain WebView to send payment request")
    }
    
    // 응답 대기 중인 요청들
    private val pendingRequests = mutableMapOf<String, WebView>()
    
    /**
     * 블록체인에서 결제 응답 전송
     */
    fun sendPaymentResponse(requestId: String, responseData: JSONObject) {
        pendingRequests[requestId]?.let { appWebView ->
            val script = """
                (function() {
                    const event = new CustomEvent('paymentResponse', {
                        detail: ${responseData.toString()}
                    });
                    window.dispatchEvent(event);
                })();
            """.trimIndent()
            
            appWebView.evaluateJavascript(script) { result ->
                Log.d(TAG, "Payment response sent back to app: $result")
            }
            
            // 처리 완료 후 제거
            pendingRequests.remove(requestId)
        }
    }
    
    /**
     * 현재 활성화된 블록체인 WebView 가져오기
     */
    fun getActiveBlockchainWebView(): WebView? = activeBlockchainWebView
    
    /**
     * 현재 활성화된 앱 WebView 가져오기
     */
    fun getActiveAppWebView(): WebView? = activeAppWebView
    
    
    /**
     * WebView 생성 헬퍼
     */
    private fun createWebView(manifest: MiniAppManifest): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
            }
        }
    }
    
    /**
     * 모든 WebView 정리
     */
    fun cleanup() {
        activeBlockchainWebView?.destroy()
        activeBlockchainWebView = null
        activeBlockchainId = null
        
        activeAppWebView?.destroy()
        activeAppWebView = null
        activeAppId = null
        
        _activeBlockchain.value = null
    }
}