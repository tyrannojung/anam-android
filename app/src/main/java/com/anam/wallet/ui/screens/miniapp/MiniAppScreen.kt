package com.anam.wallet.ui.screens.miniapp

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.anam.wallet.miniapp.MiniAppJavaScriptBridge
import com.anam.wallet.miniapp.MiniAppLoader
import com.anam.wallet.miniapp.MiniAppManifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.anam.wallet.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniAppScreen(
    appId: String
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    var manifest by remember { mutableStateOf<MiniAppManifest?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    DisposableEffect(appId) {
        val loader = MiniAppLoader(context)
        manifest = loader.loadMiniApp(appId)
        
        onDispose {
            // Trigger onHide lifecycle event before destroying
            webView?.evaluateJavascript("if(typeof App !== 'undefined' && App.onHide) App.onHide();", null)
            webView?.destroy()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (appId) {
                            "government24" -> "앱 모듈"
                            "ethereum" -> "블록체인 모듈"
                            else -> manifest?.name ?: "Mini App"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            manifest?.let { miniApp ->
                MiniAppWebView(
                    appId = appId,
                    manifest = miniApp,
                    onWebViewCreated = { webView = it }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiniAppWebView(
    appId: String,
    manifest: MiniAppManifest,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    val loader = remember { MiniAppLoader(context) }
    val basePath = remember { loader.getMiniAppBasePath(appId) }
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    
                    // Enable debugging in debug builds
                    WebView.setWebContentsDebuggingEnabled(true)
                }
                
                webViewClient = MiniAppWebViewClient(
                    onPageFinishedCallback = { view ->
                        // Trigger lifecycle events after page loads
                        view.evaluateJavascript("if(typeof App !== 'undefined' && App.onLaunch) App.onLaunch();", null)
                        view.evaluateJavascript("if(typeof App !== 'undefined' && App.onShow) App.onShow();", null)
                    }
                )
                webChromeClient = MiniAppWebChromeClient()
                
                // Add JavaScript Bridge
                addJavascriptInterface(
                    MiniAppJavaScriptBridge(ctx, manifest),
                    "anam"
                )
                
                // Load the first page
                val firstPage = manifest.pages.firstOrNull() ?: "pages/index/index"
                val url = "$basePath${firstPage}.html"
                loadUrl(url)
                
                onWebViewCreated(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private class MiniAppWebViewClient(
    private val onPageFinishedCallback: ((WebView) -> Unit)? = null
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        
        // Allow navigation within the mini app
        if (url.startsWith("file://")) {
            return false
        }
        
        // Block external URLs for security
        Log.w("MiniAppWebView", "Blocked external URL: $url")
        return true
    }
    
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d("MiniAppWebView", "Page loaded: $url")
        view?.let { onPageFinishedCallback?.invoke(it) }
    }
}

private class MiniAppWebChromeClient : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        consoleMessage?.let {
            Log.d(
                "MiniAppConsole",
                "[${it.sourceId()}:${it.lineNumber()}] ${it.message()}"
            )
        }
        return true
    }
}