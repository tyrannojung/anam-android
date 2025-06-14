package com.anam.wallet.blockchain

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import com.anam.wallet.R
import com.anam.wallet.miniapp.MiniAppLoader
import com.anam.wallet.miniapp.CustomSchemeWebViewClient
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

/**
 * Blockchain Service running in a separate process
 * Manages blockchain WebViews and handles IPC communication
 */
class BlockchainService : Service() {
    companion object {
        private const val TAG = "BlockchainService"
        private const val CHANNEL_ID = "blockchain_service"
        private const val NOTIFICATION_ID = 1001
    }

    private val binder = BlockchainServiceImpl()
    private var activeBlockchainWebView: WebView? = null
    private var activeBlockchainId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val pendingCallbacks = mutableMapOf<String, IBlockchainCallback>()

    // 첫 번째 startService() 또는 bindService() 호출 시
    // → onCreate() 호출 (딱 한 번만!)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BlockchainService onCreate")
        
        // Set WebView data directory for blockchain process
        // Android 9 (API 28) 이상에서는 여러 프로세스가 동일한 WebView 데이터 디렉토리를
        // 사용할 수 없으므로, 각 프로세스마다 고유한 suffix를 설정해야 함
        // 
        // 문제: 메인 프로세스와 블록체인 프로세스가 동일한 /app_webview/ 디렉토리 사용 시 충돌
        // 해결: 블록체인 프로세스는 /app_webview_blockchain/ 디렉토리 사용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName()
            Log.d(TAG, "Current process: $processName")
            if (processName != null && processName.endsWith(":blockchain")) {
                WebView.setDataDirectorySuffix("blockchain")
                Log.d(TAG, "WebView data directory suffix set to 'blockchain'")
            }
        }
        
        createNotificationChannel()
        // ForegroundService로 승격
        startForeground(NOTIFICATION_ID, createNotification())
        // → 상태바에 알림 표시됨
        // → 시스템이 서비스를 죽이지 않음
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "BlockchainService onBind")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "BlockchainService onDestroy")
        activeBlockchainWebView?.destroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Blockchain Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Blockchain operations running in background"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, BlockchainUIActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Anam Wallet")
            .setContentText("Blockchain service is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * 블록체인별 WebView를 생성하는 함수
     *
     * AIDL 호출 시 주의사항:
     * - BlockchainService는 AIDL 요청을 Binder Thread Pool에서 처리함
     * - WebView는 반드시 메인 스레드에서만 생성/조작 가능
     * - 따라서 handler.post()로 메인 스레드로 전환 필요
     *
     * 스레드 흐름:
     * 1. AIDL 요청 → Binder Thread에서 수신
     * 2. handler.post() → 메인 스레드로 작업 전달
     * 3. 메인 스레드에서 WebView 생성 및 설정
     */
    private fun createBlockchainWebView(blockchainId: String) {
        // Binder Thread → Main Thread 전환
        // WebView 생성 가능
        handler.post {
            Log.d(TAG, "Creating WebView for blockchain: $blockchainId")
            
            // 기존 웹뷰 정리
            activeBlockchainWebView?.destroy()
            
            // Create new WebView
            val webView = WebView(applicationContext).apply {
                settings.apply {
                    javaScriptEnabled = true      // JavaScript 실행 허용 (필수!)
                    domStorageEnabled = true      // localStorage/sessionStorage 사용

                    // 파일 접근 권한 (커스텀 스킴 사용으로 file:// 차단)
                    allowFileAccess = false        // file:// URL 접근 차단
                    allowContentAccess = false     // content:// URL 접근 차단

                    // 화면 표시 설정
                    setSupportZoom(false)         // 손가락으로 줌 비활성화
                    loadWithOverviewMode = true   // 페이지를 화면 너비에 맞춤
                    useWideViewPort = true        // HTML viewport 태그 지원
                }
                
                // WebViewClient는 manifest 로드 후에 설정
                
                // Add JavaScript bridge
                // JavaScript-Android 간 통신 브릿지 설정
                // JS에서 'anam' 객체를 통해 Android 메서드 호출 가능
                // 사용법: anam.sendPaymentResponse(...)
                addJavascriptInterface(BlockchainJSBridge(), "anam")
            }
            
            // Load blockchain mini-app
            val loader = MiniAppLoader(applicationContext)

            // 비동기 함수를 동기적으로 기다림
            val manifest = runBlocking {
                // suspend 함수 호출 가능
                // 완료될 때까지 기다림
                loader.loadMiniApp(blockchainId)
            }
            if (manifest != null) {
                // 기본 경로 가져오기
                val basePath = loader.getMiniAppBasePath(blockchainId)
                
                // CustomSchemeWebViewClient 생성 및 설정
                Log.d(TAG, "Setting CustomSchemeWebViewClient for blockchain")
                Log.d(TAG, "BasePath: $basePath")
                
                webView.webViewClient = CustomSchemeWebViewClient(
                    appId = blockchainId,
                    basePath = basePath,
                    manifest = manifest,
                    onPageFinishedCallback = { view ->
                        Log.d(TAG, "Blockchain WebView loaded")
                        // Trigger lifecycle events
                        view.evaluateJavascript("if(typeof App !== 'undefined' && App.onLaunch) App.onLaunch();", null)
                        view.evaluateJavascript("if(typeof App !== 'undefined' && App.onShow) App.onShow();", null)
                    }
                )

                // 첫 페이지 결정
                val firstPage = manifest.pages.firstOrNull() ?: "pages/index/index"
                
                // 커스텀 스킴 URL 사용
                // 기존: "file:///data/.../ethereum/pages/index/index.html"
                // 변경: "anam://miniapp-com.anam.ethereum/pages/index/index.html"
                val url = "anam://miniapp-$blockchainId/${firstPage}.html"
                
                Log.d(TAG, "Loading blockchain URL: $url")
                webView.loadUrl(url)

                // 변수 저장
                activeBlockchainWebView = webView
                activeBlockchainId = blockchainId
            } else {
                Log.e(TAG, "Failed to load blockchain manifest: $blockchainId")
            }
        }
    }

    /**
     * JavaScript bridge for blockchain WebView
     */
    inner class BlockchainJSBridge {
        @JavascriptInterface
        fun sendPaymentResponse(requestId: String, responseJson: String) {
            Log.d(TAG, "sendPaymentResponse: requestId=$requestId")
            
            handler.post {
                val callback = pendingCallbacks.remove(requestId)
                if (callback != null) {
                    try {
                        callback.onSuccess(responseJson)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send success callback", e)
                    }
                } else {
                    Log.w(TAG, "No callback found for requestId: $requestId")
                }
            }
        }
        
        @JavascriptInterface
        fun sendPaymentError(requestId: String, error: String) {
            Log.d(TAG, "sendPaymentError: requestId=$requestId, error=$error")
            
            handler.post {
                val callback = pendingCallbacks.remove(requestId)
                if (callback != null) {
                    try {
                        callback.onError(error)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send error callback", e)
                    }
                } else {
                    Log.w(TAG, "No callback found for requestId: $requestId")
                }
            }
        }
        
        @JavascriptInterface
        fun getWalletAddress(): String {
            // This would be called synchronously from blockchain WebView
            return "0x1234567890abcdef" // Placeholder
        }
    }

    /**
     * AIDL service implementation
     */
    inner class BlockchainServiceImpl : IBlockchainService.Stub() {
        override fun processRequest(requestJson: String?, callback: IBlockchainCallback?) {
            Log.d(TAG, "processRequest: $requestJson")
            Log.d(TAG, "activeBlockchainWebView is null: ${activeBlockchainWebView == null}")
            Log.d(TAG, "activeBlockchainId: $activeBlockchainId")
            
            if (requestJson == null || callback == null) {
                callback?.onError("Invalid request or callback")
                return
            }
            
            if (activeBlockchainWebView == null) {
                Log.e(TAG, "No active blockchain WebView! activeBlockchainId=$activeBlockchainId")
                callback.onError("No active blockchain")
                return
            }
            
            handler.post {
                try {
                    // Parse request to get existing requestId
                    val requestData = JSONObject(requestJson)
                    val requestId = requestData.optString("requestId")
                    
                    if (requestId.isEmpty()) {
                        Log.e(TAG, "No requestId found in request")
                        callback.onError("No requestId found")
                        return@post
                    }
                    
                    // Store callback
                    pendingCallbacks[requestId] = callback
                    
                    // Send to blockchain WebView
                    val script = """
                        (function() {
                            const event = new CustomEvent('paymentRequest', {
                                detail: ${requestData.toString()}
                            });
                            window.dispatchEvent(event);
                        })();
                    """.trimIndent()
                    
                    activeBlockchainWebView?.evaluateJavascript(script) { result ->
                        Log.d(TAG, "Event dispatched to blockchain: $result")
                    }
                    
                    // Set timeout to clean up if no response
                    handler.postDelayed({
                        if (pendingCallbacks.containsKey(requestId)) {
                            pendingCallbacks.remove(requestId)
                            try {
                                callback.onError("Request timeout")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to send timeout error", e)
                            }
                        }
                    }, 30000) // 30 seconds timeout
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process request", e)
                    callback.onError("Failed to process request: ${e.message}")
                }
            }
        }
        
        override fun getWalletAddress(): String? {
            return activeBlockchainWebView?.let { webView ->
                var address: String? = null
                val latch = java.util.concurrent.CountDownLatch(1)
                
                handler.post {
                    webView.evaluateJavascript("anam.getWalletAddress()") { result ->
                        address = result?.trim('"')
                        latch.countDown()
                    }
                }
                
                try {
                    latch.await(5, java.util.concurrent.TimeUnit.SECONDS)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get wallet address", e)
                }
                
                address
            }
        }
        
        override fun switchBlockchain(blockchainId: String?) {
            Log.d(TAG, "switchBlockchain: $blockchainId")
            Log.d(TAG, "Current activeBlockchainId: $activeBlockchainId")
            
            if (blockchainId.isNullOrEmpty()) {
                Log.w(TAG, "switchBlockchain called with null or empty blockchainId")
                return
            }
            
            if (blockchainId != activeBlockchainId) {
                Log.d(TAG, "Switching from $activeBlockchainId to $blockchainId")
                createBlockchainWebView(blockchainId)
            } else {
                Log.d(TAG, "Already on blockchain: $blockchainId")
            }
        }
        
        override fun isBlockchainActive(): Boolean {
            return activeBlockchainWebView != null
        }
        
        override fun getActiveBlockchainId(): String? {
            return this@BlockchainService.activeBlockchainId
        }
    }
    
    /**
     * Get the current process name
     */
    private fun getProcessName(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            try {
                val pid = android.os.Process.myPid()
                val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val processes = manager.runningAppProcesses
                processes?.find { it.pid == pid }?.processName
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get process name", e)
                null
            }
        }
    }
}