package com.anam.wallet.miniapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import com.anam.wallet.model.miniapp.MiniAppManifest
import com.anam.wallet.blockchain.BlockchainService
import com.anam.wallet.blockchain.api.IBlockchainApi
import com.anam.wallet.blockchain.api.IBlockchainCallback
import com.anam.wallet.blockchain.internal.IBlockchainManager
import java.io.File

/**
 * 미니앱 관리자 - WebView Pool 관리 및 미니앱 간 통신 담당
 */
class MiniAppManager private constructor(private val context: Context) {
    
    // Activity context를 저장하기 위한 변수
    private var activityContext: Context? = null
    
    companion object {
        private const val TAG = "MiniAppManager"
        private const val PREF_NAME = "miniapp_prefs"
        private const val KEY_LAST_BLOCKCHAIN = "last_active_blockchain"
        private const val DEFAULT_BLOCKCHAIN = "com.anam.ethereum"
        
        @Volatile
        private var INSTANCE: MiniAppManager? = null
        
        fun getInstance(context: Context): MiniAppManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MiniAppManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // SharedPreferences for storing last active blockchain
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    // AIDL 서비스 연결
    private var blockchainApi: IBlockchainApi? = null
    private var blockchainManager: IBlockchainManager? = null
    private var managerConnection: ServiceConnection? = null
    private var apiConnection: ServiceConnection? = null
    private var isServiceBound = false
    
    // 활성 앱 WebView (포그라운드에서만 유지)
    private var activeAppWebView: WebView? = null
    private var activeAppId: String? = null
    
    // 미니앱 로더
    private val miniAppLoader = MiniAppLoader(context)
    
    // 상태 관리
    private val _activeBlockchain = MutableStateFlow<String?>(null)
    val activeBlockchain: StateFlow<String?> = _activeBlockchain
    
    // 서비스 연결 대기 중인 블록체인 ID
    private var pendingBlockchainId: String? = null
    
    init {
        bindToBlockchainService()
        bindToBlockchainApi()
        initializeDefaultBlockchain()
    }
    
    /**
     * 초기 블록체인 활성화
     */
    private fun initializeDefaultBlockchain() {
        // 마지막 활성화된 블록체인 ID 가져오기
        val lastBlockchainId = prefs.getString(KEY_LAST_BLOCKCHAIN, null)
        
        if (lastBlockchainId != null) {
            // 이전에 사용한 블록체인이 있으면 그것을 활성화
            Log.d(TAG, "Restoring last active blockchain: $lastBlockchainId")
            pendingBlockchainId = lastBlockchainId
        } else {
            // 처음 실행이면 기본값(이더리움) 활성화
            Log.d(TAG, "First run, activating default blockchain: $DEFAULT_BLOCKCHAIN")
            pendingBlockchainId = DEFAULT_BLOCKCHAIN
        }
    }
    
    /**
     * 블록체인 서비스에 바인드
     */
    private fun bindToBlockchainService() {
        // intent "어떤 작업을 하고 싶다"는 요청
        // context: 현재 위치 (어디서 출발하는지)
        // BlockchainService::class.java: 목적지 (어디로 가고 싶은지)
        // "BlockchainService와 연결하고 싶어"라는 의도 생성
        val intent = Intent(context, BlockchainService::class.java)

        // ServiceConnection = 서비스와 연결될 때의 "이벤트 리스너"
        // bindService() 호출 후 연결 성공 시 onServiceConnected 자동 호출
        // 블록체인 서비스 크래시시 onServiceDisconnected 자동 호출
        managerConnection = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?, // 각각의 인자는 자동으로 채워줌
                service: IBinder? // 각각의 인자는 자동으로 채워줌
            ) {
                // 서비스랑 통신할 수 있는 리모컨을 받음 (인터페이스, 이제부터 블록체인 서비스와 통신 가능)
                blockchainManager = IBlockchainManager.Stub.asInterface(service)
                isServiceBound = true
                Log.d(TAG, "Connected to BlockchainService (Manager)")
                
                // 블록체인 변경 리스너 등록
                registerBlockchainChangeListener()
                
                // 대기 중인 블록체인 활성화 요청이 있으면 처리
                pendingBlockchainId?.let { blockchainId ->
                    Log.d(TAG, "Processing pending blockchain activation: $blockchainId")
                    try {
                        blockchainManager?.switchBlockchain(blockchainId)
                        _activeBlockchain.value = blockchainId
                        
                        // 활성화된 블록체인 ID 저장
                        prefs.edit().putString(KEY_LAST_BLOCKCHAIN, blockchainId).apply()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to activate pending blockchain", e)
                    }
                    pendingBlockchainId = null
                }
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                // 리스너 해제
                unregisterBlockchainChangeListener()
                blockchainManager = null
                isServiceBound = false
                Log.d(TAG, "Disconnected from BlockchainService")
            }
        }
        
        // 서비스를 먼저 시작한다.
        context.startService(intent)
        
        // Manager 인터페이스로 바인드 (시스템 관리용)
        val managerIntent = Intent(context, BlockchainService::class.java).apply {
            action = "com.anam.wallet.blockchain.MANAGER"
        }
        context.bindService(managerIntent, managerConnection!!, Context.BIND_AUTO_CREATE)
        
        // API 인터페이스도 바인드
        bindToBlockchainApi()
    }
    
    /**
     * 블록체인 API 서비스에 바인드
     */
    private fun bindToBlockchainApi() {
        val intent = Intent(context, BlockchainService::class.java).apply {
            action = "com.anam.wallet.blockchain.API"
        }
        
        apiConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                blockchainApi = IBlockchainApi.Stub.asInterface(service)
                Log.d(TAG, "Connected to BlockchainService (API)")
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                blockchainApi = null
                Log.d(TAG, "Disconnected from BlockchainService (API)")
            }
        }
        
        context.bindService(intent, apiConnection!!, Context.BIND_AUTO_CREATE)
    }
    
    /**
     * 블록체인 미니앱 활성화
     */
    fun activateBlockchain(blockchainId: String) {
        Log.d(TAG, "Activating blockchain: $blockchainId")
        
        // 이미 활성화된 블록체인이면 무시
        if (_activeBlockchain.value == blockchainId) {
            Log.d(TAG, "Blockchain already active: $blockchainId")
            return
        }
        
        if (!isServiceBound || blockchainManager == null) {
            Log.d(TAG, "BlockchainService not connected yet, queuing activation request")
            pendingBlockchainId = blockchainId
            return
        }
        
        // AIDL을 통해 블록체인 서비스에 전환 요청
        try {
            blockchainManager?.switchBlockchain(blockchainId)
            _activeBlockchain.value = blockchainId
            
            // 활성화된 블록체인 ID 저장
            prefs.edit().putString(KEY_LAST_BLOCKCHAIN, blockchainId).apply()
            
            Log.d(TAG, "Blockchain switched to: $blockchainId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to switch blockchain", e)
        }
    }
    
    /**
     * Activity context 설정
     */
    fun setActivityContext(activityContext: Context) {
        this.activityContext = activityContext
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
                // JavaScript Bridge 추가 - 결제 요청 콜백 포함
                val bridge = MiniAppJavaScriptBridge(
                    context = activityContext ?: context,  // Activity context 우선 사용
                    manifest = manifest,
                    onPaymentRequest = { paymentData, _ ->
                        // 앱에서 결제 요청 시 블록체인으로 전달
                        sendPaymentToBlockchain(paymentData, this)
                    },
                    onPaymentResponse = null, // 앱은 응답을 받는 쪽이므로 null
                    onVPRequest = { vpRequest ->
                        // VP 요청 처리
                        handleVPRequest(vpRequest, this)
                    },
                    webView = this  // WebView 참조 전달
                )
                addJavascriptInterface(bridge, "anam")
                
                // WebViewClient 설정 - AssetLoaderWebViewClient 사용
                val loader = MiniAppLoader(context)
                val basePath = loader.getMiniAppBasePath(appId)
                
                webViewClient = AssetLoaderWebViewClient(
                    appId = appId,
                    basePath = basePath,
                    manifest = manifest,  // 페이지 화이트리스트 검증용
                    onPageFinishedCallback = { view ->
                        Log.d(TAG, "Page finished loading")
                        
                        // 생명주기 함수 호출
                        view.evaluateJavascript("console.log('MiniAppManager: Page loaded, checking App object...');", null)
                        view.evaluateJavascript("if(typeof App !== 'undefined' && App.onLaunch) { console.log('MiniAppManager: Calling App.onLaunch()'); App.onLaunch(); }", null)
                        view.evaluateJavascript("if(typeof App !== 'undefined' && App.onShow) { console.log('MiniAppManager: Calling App.onShow()'); App.onShow(); }", null)
                    }
                )
                
                // WebChromeClient 설정 - 콘솔 로그 출력
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d(TAG, "[Console] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                        }
                        return true
                    }
                }
                
                // 메인 페이지 로드 - WebViewAssetLoader URL 사용
                val firstPage = manifest.pages.firstOrNull() ?: "pages/index/index"
                val baseUrl = AssetLoaderWebViewClient.getBaseUrlForApp(appId)
                val url = "$baseUrl${firstPage}.html"
                
                Log.d(TAG, "Loading app URL: $url")
                loadUrl(url)
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
        
        if (!isServiceBound || blockchainApi == null) {
            Log.e(TAG, "BlockchainService not connected")
            return
        }
        
        // 요청 ID 생성 (응답 매칭용)
        // 비동기 통신이므로, 결제가 동시에 여러개 보낼 수 있으므로 응답 id를 매칭하는게 중요
        val requestId = "req_${System.currentTimeMillis()}"
        paymentData.put("requestId", requestId)
        
        // 앱 WebView 저장 (응답 전달용)
        pendingRequests[requestId] = appWebView
        
        try {
            // AIDL을 통해 블록체인 서비스로 요청 전송
            blockchainApi?.processRequest(paymentData.toString(), object : IBlockchainCallback.Stub() {

                // 성공시,
                override fun onSuccess(responseJson: String?) {
                    Log.d(TAG, "Blockchain request success: $responseJson")
                    
                    // UI 스레드에서 WebView 업데이트
                    appWebView.post {
                        try {
                            // JSONObject, 문자열을 JSON 객체로 변환
                            val responseData = JSONObject(responseJson ?: "{}")
                            //  JavaScript 코드를 문자열로 생성
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
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to process blockchain response", e)
                        }
                    }
                    
                    // 처리 완료 후 제거
                    pendingRequests.remove(requestId)
                }
                
                override fun onError(errorMessage: String?) {
                    Log.e(TAG, "Blockchain request error: $errorMessage")
                    
                    // UI 스레드에서 에러 처리
                    appWebView.post {
                        val errorData = JSONObject().apply {
                            put("error", errorMessage ?: "Unknown error")
                            put("requestId", requestId)
                        }
                        
                        val script = """
                            (function() {
                                const event = new CustomEvent('paymentError', {
                                    detail: ${errorData.toString()}
                                });
                                window.dispatchEvent(event);
                            })();
                        """.trimIndent()
                        
                        appWebView.evaluateJavascript(script, null)
                    }
                    
                    // 처리 완료 후 제거
                    pendingRequests.remove(requestId)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send request to blockchain service", e)
            pendingRequests.remove(requestId)
        }
    }
    
    // 응답 대기 중인 요청들
    private val pendingRequests = mutableMapOf<String, WebView>()
    
    
    /**
     * 현재 활성화된 블록체인 ID 가져오기
     */
    fun getActiveBlockchainId(): String? {
        return try {
            blockchainManager?.getActiveBlockchainId()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get active blockchain ID", e)
            null
        }
    }
    
    /**
     * 현재 활성화된 앱 WebView 가져오기
     */
    fun getActiveAppWebView(): WebView? = activeAppWebView
    
    
    /**
     * WebView 생성 헬퍼
     * 
     * 보안 설정:
     * - 커스텀 스킴(anam://)을 사용하므로 file:// 접근 차단
     * - 심층 방어를 위해 모든 file:// 관련 권한 비활성화
     */
    private fun createWebView(manifest: MiniAppManifest): WebView {
        // Activity context가 있으면 사용, 없으면 application context 사용
        val webViewContext = activityContext ?: context
        return WebView(webViewContext).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                
                // 파일 접근 설정
                allowFileAccess = false  // file:// URL 접근 차단
                allowContentAccess = false  // content:// URL 접근 차단
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
                
                // Mixed Content 정책 - HTTPS에서 HTTP 리소스 차단
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                
                // 추가 보안 설정
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                
                // 디버깅 (개발 중에만)
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }
    }
    
    /**
     * VP 요청 처리
     */
    private fun handleVPRequest(vpRequest: JSONObject, appWebView: WebView) {
        Log.d(TAG, "handleVPRequest called with: $vpRequest")
        
        try {
            val challenge = vpRequest.getString("challenge")
            val requesterName = vpRequest.optString("requesterName", "Unknown Service")
            
            // VP 콜백이 설정되어 있으면 호출 (Activity에서 처리)
            if (vpCallback != null) {
                vpCallback?.invoke(vpRequest, appWebView)
            } else {
                Log.e(TAG, "No VP callback registered")
                sendVPError(appWebView, "Internal error: No VP handler")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle VP request", e)
            sendVPError(appWebView, e.message ?: "Unknown error")
        }
    }
    
    /**
     * VP 에러 응답 전송
     */
    private fun sendVPError(webView: WebView, error: String) {
        val errorData = JSONObject().apply {
            put("error", error)
        }
        
        val script = """
            (function() {
                const event = new CustomEvent('vpResponse', {
                    detail: ${errorData.toString()}
                });
                window.dispatchEvent(event);
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(script, null)
    }
    
    /**
     * VP 콜백 설정
     */
    fun setVPCallback(callback: (JSONObject, WebView) -> Unit) {
        vpCallback = callback
    }
    
    private var vpCallback: ((JSONObject, WebView) -> Unit)? = null
    
    /**
     * 블록체인 변경 리스너
     */
    private val blockchainChangeListener = object : IBlockchainCallback.Stub() {
        override fun onSuccess(result: String?) {
            // result는 변경된 블록체인 ID
            result?.let { blockchainId ->
                Log.d(TAG, "Received blockchain change notification: $blockchainId")
                _activeBlockchain.value = blockchainId
                // SharedPreferences에도 저장
                prefs.edit().putString(KEY_LAST_BLOCKCHAIN, blockchainId).apply()
                Log.d(TAG, "Updated activeBlockchain to: $blockchainId")
            }
        }
        
        override fun onError(error: String?) {
            Log.e(TAG, "Blockchain change error: $error")
        }
    }
    
    /**
     * 블록체인 변경 리스너 등록
     */
    private fun registerBlockchainChangeListener() {
        try {
            blockchainManager?.registerBlockchainChangeListener(blockchainChangeListener)
            Log.d(TAG, "Registered blockchain change listener")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register blockchain change listener", e)
        }
    }
    
    /**
     * 블록체인 변경 리스너 해제
     */
    private fun unregisterBlockchainChangeListener() {
        try {
            blockchainManager?.unregisterBlockchainChangeListener(blockchainChangeListener)
            Log.d(TAG, "Unregistered blockchain change listener")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister blockchain change listener", e)
        }
    }
    
    /**
     * 모든 WebView 정리
     */
    fun cleanup() {
        // Unregister blockchain change listener
        unregisterBlockchainChangeListener()
        
        // Unbind from blockchain service
        if (isServiceBound) {
            managerConnection?.let {
                context.unbindService(it)
            }
            apiConnection?.let {
                context.unbindService(it)
            }
            isServiceBound = false
        }
        
        activeAppWebView?.destroy()
        activeAppWebView = null
        activeAppId = null
        
        _activeBlockchain.value = null
    }
}