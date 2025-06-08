package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import org.json.JSONObject

class MiniAppJavaScriptBridge(
    private val context: Context,
    private val manifest: MiniAppManifest
) {
    companion object {
        private const val TAG = "MiniAppJSBridge"
    }
    
    @JavascriptInterface
    fun showToast(message: String) {
        Log.d(TAG, "showToast: $message")
        context.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    @JavascriptInterface
    fun getSystemInfo(): String {
        return try {
            val info = JSONObject().apply {
                put("platform", "android")
                put("version", android.os.Build.VERSION.RELEASE)
                put("brand", android.os.Build.BRAND)
                put("model", android.os.Build.MODEL)
                put("screenWidth", context.resources.displayMetrics.widthPixels)
                put("screenHeight", context.resources.displayMetrics.heightPixels)
                put("pixelRatio", context.resources.displayMetrics.density)
            }
            info.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system info", e)
            "{}"
        }
    }
    
    @JavascriptInterface
    fun getAppInfo(): String {
        return try {
            val info = JSONObject().apply {
                put("appId", manifest.appId)
                put("name", manifest.name)
                put("version", manifest.version)
                put("permissions", manifest.permissions.joinToString(","))
            }
            info.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app info", e)
            "{}"
        }
    }
    
    @JavascriptInterface
    fun navigateTo(page: String) {
        Log.d(TAG, "navigateTo: $page")
        // This will be handled by the WebView in MiniAppActivity
        context.runOnUiThread {
            // Trigger navigation through a callback or event
            // For now, just log it
            Log.d(TAG, "Navigation requested to: $page")
        }
    }
    
    @JavascriptInterface
    fun navigateBack() {
        Log.d(TAG, "navigateBack")
        // Navigation is handled by the Composable navigation
        // This would need to be implemented with a callback
    }
    
    @JavascriptInterface
    fun setNavigationBarTitle(title: String) {
        Log.d(TAG, "setNavigationBarTitle: $title")
        // This would update the top bar title
        // Implementation would require a callback to the Activity
    }
    
    @JavascriptInterface
    fun setStorageItem(key: String, value: String) {
        try {
            val prefs = context.getSharedPreferences("miniapp_${manifest.appId}", Context.MODE_PRIVATE)
            prefs.edit().putString(key, value).apply()
            Log.d(TAG, "Storage set: $key = $value")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set storage item", e)
        }
    }
    
    @JavascriptInterface
    fun getStorageItem(key: String): String? {
        return try {
            val prefs = context.getSharedPreferences("miniapp_${manifest.appId}", Context.MODE_PRIVATE)
            prefs.getString(key, null).also {
                Log.d(TAG, "Storage get: $key = $it")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get storage item", e)
            null
        }
    }
    
    @JavascriptInterface
    fun removeStorageItem(key: String) {
        try {
            val prefs = context.getSharedPreferences("miniapp_${manifest.appId}", Context.MODE_PRIVATE)
            prefs.edit().remove(key).apply()
            Log.d(TAG, "Storage removed: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove storage item", e)
        }
    }
    
    @JavascriptInterface
    fun clearStorage() {
        try {
            val prefs = context.getSharedPreferences("miniapp_${manifest.appId}", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Log.d(TAG, "Storage cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear storage", e)
        }
    }
    
    @JavascriptInterface
    fun requestVP(vpType: String, callback: String) {
        Log.d(TAG, "requestVP: type=$vpType, callback=$callback")
        // This would request a Verifiable Presentation from the wallet
        // For now, return a mock response
        context.runOnUiThread {
            // In a real implementation, this would:
            // 1. Check if the mini-app has permission to request this VP type
            // 2. Show a user consent dialog
            // 3. Generate or retrieve the VP
            // 4. Call the JavaScript callback with the result
            
            val mockVP = JSONObject().apply {
                put("type", vpType)
                put("status", "success")
                put("data", JSONObject().apply {
                    put("credential", "mock_credential_data")
                    put("issuer", "AnamWallet")
                    put("subject", "user_did")
                })
            }
            
            // Execute callback in WebView
            // This would need access to the WebView instance
            Log.d(TAG, "VP response: ${mockVP.toString()}")
        }
    }
    
    private fun Context.runOnUiThread(action: () -> Unit) {
        if (this is android.app.Activity) {
            runOnUiThread(action)
        }
    }
}