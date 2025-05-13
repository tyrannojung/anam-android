package com.anam.wallet.ui.screens.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

// Global variables to preserve browser state across tab changes
private var cachedWebView: WebView? = null
private var lastVisitedUrl: String = "https://www.google.com"
private var webViewState: Bundle? = null

@Composable
fun BrowserScreen() {
    // Use the last visited URL as initial, or Google if it's the first time
    val initialUrl = lastVisitedUrl
    var url by remember { mutableStateOf(initialUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // URL input/search bar with black background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            TextField(
                value = url,
                onValueChange = { 
                    url = it
                    isEditing = true
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        url = processInput(url)
                        isEditing = false
                        focusManager.clearFocus()
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        url = processInput(url)
                        isEditing = false
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        }
        
        // WebView with loading indicator
        Box(modifier = Modifier.fillMaxSize()) {
            val webView = remember { mutableStateOf<WebView?>(cachedWebView) }
            
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    // Use the cached WebView or create a new one
                    if (cachedWebView != null) {
                        // Return the cached WebView
                        cachedWebView!!.apply {
                            // Restore state if available
                            if (webViewState != null) {
                                restoreState(webViewState!!)
                            }
                            
                            if (url != lastVisitedUrl) {
                                loadUrl(url)
                            }
                        }
                    } else {
                        // Create a new WebView
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                // Enable caching
                                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                                // Enable persistent storage
                                databaseEnabled = true
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, urlString: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, urlString, favicon)
                                    isLoading = true
                                    
                                    // Only update URL if we're not in editing mode and it's not about:blank
                                    if (!isEditing && urlString != null && urlString != "about:blank") {
                                        url = urlString
                                        // Save the URL for persistence
                                        lastVisitedUrl = urlString
                                    }
                                }

                                override fun onPageFinished(view: WebView?, urlString: String?) {
                                    super.onPageFinished(view, urlString)
                                    isLoading = false
                                    
                                    // Save the WebView state after page load completes
                                    if (view != null) {
                                        val state = Bundle()
                                        view.saveState(state)
                                        webViewState = state
                                    }
                                }
                                
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false // Let WebView handle URLs
                                }
                            }
                            
                            loadUrl(initialUrl)
                            
                            // Save as cached WebView
                            cachedWebView = this
                        }
                    }
                },
                update = { view ->
                    // Only update the WebView if the URL has changed and we're not in editing mode
                    if (!isEditing && view.url != url && !url.startsWith("about:blank")) {
                        view.loadUrl(url)
                        lastVisitedUrl = url
                    }
                    
                    // Always make sure we have the latest WebView reference
                    cachedWebView = view
                    webView.value = view
                }
            )
            
            // Save state when leaving the screen, but don't destroy the WebView
            DisposableEffect(Unit) {
                onDispose {
                    val currentWebView = webView.value
                    if (currentWebView != null) {
                        // Save state
                        val state = Bundle()
                        currentWebView.saveState(state)
                        webViewState = state
                        
                        // Save the URL
                        currentWebView.url?.let { currentUrl ->
                            if (currentUrl != "about:blank") {
                                lastVisitedUrl = currentUrl
                            }
                        }
                    }
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

// Helper function to process URL input
private fun processInput(input: String): String {
    val trimmedInput = input.trim()
    
    return when {
        // If contains spaces, treat as search query
        trimmedInput.contains(" ") -> 
            "https://www.google.com/search?q=${trimmedInput.replace(" ", "+")}"
        
        // If it starts with http:// or https://, use as is
        trimmedInput.startsWith("http://") || trimmedInput.startsWith("https://") -> 
            trimmedInput
        
        // If it contains a dot (likely a domain), prepend https://
        trimmedInput.contains(".") -> 
            "https://$trimmedInput"
        
        // Otherwise, treat as search query
        else -> 
            "https://www.google.com/search?q=$trimmedInput"
    }
}