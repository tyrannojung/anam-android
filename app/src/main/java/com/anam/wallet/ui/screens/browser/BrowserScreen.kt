package com.anam.wallet.ui.screens.browser

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.anam.wallet.R

// Companion object to store the last visited URL across recompositions
private object BrowserStateManager {
    var lastVisitedUrl = "https://app.uniswap.org/"
}

@Composable
fun BrowserScreen() {
    // Use the last visited URL as initial
    val initialUrl = BrowserStateManager.lastVisitedUrl
    var url by remember { mutableStateOf(initialUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    
    // Create a WebView instance that persists across recompositions
    val webView = remember { 
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                // Database storage is enabled by default in modern WebView versions
            }
            
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // URL input bar with modern style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // URL input container
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // HTTPS icon
                Icon(
                    painter = painterResource(id = R.drawable.img_web_https),
                    contentDescription = "HTTPS",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // URL input field
                BasicTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        isEditing = true
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            url = processInput(url)
                            isEditing = false
                            focusManager.clearFocus()
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Bookmark button
            Icon(
                painter = painterResource(id = R.drawable.img_web_bookmark),
                contentDescription = "Bookmark",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // TODO: Add bookmark functionality
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Add spacing between search bar and WebView
        Spacer(modifier = Modifier.height(10.dp))
        
        // WebView with loading indicator
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RectangleShape) // Ensure WebView stays within bounds
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { _ ->
                    webView.apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, urlString: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, urlString, favicon)
                                isLoading = true
                                
                                if (!isEditing && urlString != null && urlString != "about:blank") {
                                    url = urlString
                                    BrowserStateManager.lastVisitedUrl = urlString
                                }
                            }

                            override fun onPageFinished(view: WebView?, urlString: String?) {
                                super.onPageFinished(view, urlString)
                                isLoading = false
                            }
                            
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }
                        
                        loadUrl(url)
                    }
                },
                update = { view ->
                    if (!isEditing && view.url != url && !url.startsWith("about:blank")) {
                        view.loadUrl(url)
                    }
                }
            )
            
            // Save state when component is disposed
            DisposableEffect(webView) {
                onDispose {
                    // Save the URL
                    webView.url?.let { currentUrl ->
                        if (currentUrl != "about:blank") {
                            BrowserStateManager.lastVisitedUrl = currentUrl
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
        
        // Bottom navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Icon(
                painter = painterResource(
                    id = if (webView.canGoBack()) R.drawable.img_web_back_on 
                    else R.drawable.img_web_back_off
                ),
                contentDescription = "Back",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        enabled = webView.canGoBack(),
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        webView.goBack()
                    },
                tint = if (webView.canGoBack()) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            // Forward button
            Icon(
                painter = painterResource(
                    id = if (webView.canGoForward()) R.drawable.img_web_forward_on 
                    else R.drawable.img_web_forward_off
                ),
                contentDescription = "Forward",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        enabled = webView.canGoForward(),
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        webView.goForward()
                    },
                tint = if (webView.canGoForward()) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            // Bookmark list button
            Icon(
                painter = painterResource(id = R.drawable.img_web_list),
                contentDescription = "Bookmarks",
                modifier = Modifier
                    .size(28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // TODO: Show bookmarks list
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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