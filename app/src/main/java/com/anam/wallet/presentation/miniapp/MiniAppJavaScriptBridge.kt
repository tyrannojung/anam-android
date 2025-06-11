package com.anam.wallet.presentation.miniapp

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject
import com.anam.wallet.model.miniapp.MiniAppManifest

class MiniAppJavaScriptBridge(
    private val context: Context,
    private val manifest: MiniAppManifest,
    private val onPaymentRequest: ((JSONObject, String) -> Unit)? = null,
    private val onPaymentResponse: ((String, JSONObject) -> Unit)? = null
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