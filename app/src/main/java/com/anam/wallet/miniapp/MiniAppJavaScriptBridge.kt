package com.anam.wallet.miniapp

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

class MiniAppJavaScriptBridge(
    private val context: Context,
    private val manifest: MiniAppManifest,
    private val onPaymentRequest: ((JSONObject) -> Unit)? = null
) {
    companion object {
        private const val TAG = "MiniAppJSBridge"
    }
    
    @JavascriptInterface
    fun requestPayment(paymentDataJson: String, callback: String) {
        Log.d(TAG, "requestPayment: $paymentDataJson, callback: $callback")
        
        try {
            val paymentData = JSONObject(paymentDataJson)
            
            // 블록체인 WebView로 결제 요청 전달
            context.runOnUiThread {
                onPaymentRequest?.invoke(paymentData)
                
                // 임시 응답 (실제로는 블록체인 WebView에서 처리 후 콜백)
                val response = JSONObject().apply {
                    put("status", "processing")
                    put("message", "Payment request sent to blockchain")
                }
                
                // JavaScript 콜백 실행
                // 실제 구현에서는 WebView 참조가 필요
                Log.d(TAG, "Payment request processed: $response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process payment request", e)
            
            // 에러 콜백
            val errorResponse = JSONObject().apply {
                put("error", e.message)
            }
            Log.e(TAG, "Payment error: $errorResponse")
        }
    }
    
    @JavascriptInterface
    fun sendMessage(targetAppId: String, message: String, callback: String? = null) {
        Log.d(TAG, "sendMessage to $targetAppId: $message")
        
        // 미니앱 간 메시지 전송
        // 실제 구현에서는 MiniAppManager를 통해 전달
        context.runOnUiThread {
            // TODO: MiniAppManager.sendMessage(targetAppId, message)
            Log.d(TAG, "Message sent to $targetAppId")
        }
    }
    
    private fun Context.runOnUiThread(action: () -> Unit) {
        if (this is android.app.Activity) {
            runOnUiThread(action)
        }
    }
}