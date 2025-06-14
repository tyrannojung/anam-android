package com.anam.wallet.blockchain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.anam.wallet.LocalNavController
import com.anam.wallet.miniapp.MiniAppJavaScriptBridge
import com.anam.wallet.miniapp.MiniAppLoader
import com.anam.wallet.ui.theme.AnamwalletTheme

/**
 * Activity for displaying blockchain UI in the blockchain process
 * This runs in :blockchain process along with BlockchainService
 */
class BlockchainUIActivity : ComponentActivity() {
    companion object {
        private const val TAG = "BlockchainUIActivity"
        const val EXTRA_BLOCKCHAIN_ID = "blockchain_id"
    }
    
    private var blockchainService: IBlockchainService? = null
    private var serviceConnection: ServiceConnection? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intent에서 블록체인 ID 추출
        val blockchainId = intent.getStringExtra(EXTRA_BLOCKCHAIN_ID) ?: "com.anam.ethereum"

        // BlockchainService에 바인딩
        startAndBindService()

        // Compose UI 설정
        setContent {
            AnamwalletTheme {
                BlockchainScreen(
                    blockchainId = blockchainId,
                    onBack = { finish() }
                )
            }
        }
    }
    
    private fun startAndBindService() {
        // Start the service
        val serviceIntent = Intent(this, BlockchainService::class.java)
        startService(serviceIntent)
        
        // Bind to the service
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // IBinder를 IBlockchainService 인터페이스로 변환
                blockchainService = IBlockchainService.Stub.asInterface(service)
                Log.d(TAG, "Connected to BlockchainService")
                
                // Switch to the requested blockchain
                val blockchainId = intent.getStringExtra(EXTRA_BLOCKCHAIN_ID)
                if (blockchainId != null) {
                    blockchainService?.switchBlockchain(blockchainId)
                }
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                blockchainService = null
                Log.d(TAG, "Disconnected from BlockchainService")
            }
        }
        
        serviceConnection = connection
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let {
            unbindService(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockchainScreen(
    blockchainId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var manifest by remember { mutableStateOf<com.anam.wallet.model.miniapp.MiniAppManifest?>(null) }
    
    LaunchedEffect(blockchainId) {
        val loader = MiniAppLoader(context)
        manifest = loader.loadMiniApp(blockchainId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = manifest?.name ?: "Blockchain",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (manifest != null) {
                BlockchainWebView(
                    blockchainId = blockchainId,
                    manifest = manifest!!
                )
            }
        }
    }
}

@Composable
fun BlockchainWebView(
    blockchainId: String,
    manifest: com.anam.wallet.model.miniapp.MiniAppManifest
) {
    val context = LocalContext.current
    val loader = remember { MiniAppLoader(context) }
    val basePath = remember { loader.getMiniAppBasePath(blockchainId) }
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    
                    // 파일 접근 차단 (커스텀 스킴 사용)
                    allowFileAccess = false
                    allowContentAccess = false
                    
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    
                    // Enable debugging
                    WebView.setWebContentsDebuggingEnabled(true)
                }
                
                // 커스텀 스킴을 사용하도록 설정
                webViewClient = com.anam.wallet.miniapp.CustomSchemeWebViewClient(
                    appId = blockchainId,
                    basePath = basePath,
                    manifest = manifest,
                    onPageFinishedCallback = { view ->
                        Log.d("BlockchainWebView", "Page loaded")
                        // Note: Lifecycle events are already handled by BlockchainService
                        // This is just for UI display
                    }
                )
                
                // Add JavaScript Bridge for UI interactions
                val bridge = MiniAppJavaScriptBridge(
                    context = ctx,
                    manifest = manifest,
                    onPaymentRequest = null,
                    onPaymentResponse = null
                )
                addJavascriptInterface(bridge, "anam")
                
                // Load the first page with custom scheme
                val firstPage = manifest.pages.firstOrNull() ?: "pages/index/index"
                val url = "anam://miniapp-$blockchainId/${firstPage}.html"
                
                Log.d("BlockchainWebView", "Loading URL: $url")
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}