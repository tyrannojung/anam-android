package com.anam.wallet.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.anam.wallet.ui.components.AuthBottomSheet
import com.anam.wallet.ui.theme.AnamwalletTheme

/**
 * VP 요청 시 사용자 동의를 받기 위한 투명 Activity
 * MainAppService에서 실행되어 AuthBottomSheet를 표시
 */
class AuthRequestActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_REQUESTER_NAME = "requester_name"
        const val EXTRA_CHALLENGE = "challenge"
        const val EXTRA_PRESENTATION_DEFINITION = "presentation_definition"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val requestId = intent.getStringExtra("request_id") ?: ""
        val requesterName = intent.getStringExtra(EXTRA_REQUESTER_NAME) ?: "Unknown App"
        
        setContent {
            AnamwalletTheme {
                var showSheet by remember { mutableStateOf(true) }
                
                if (showSheet) {
                    AuthBottomSheet(
                        requesterName = requesterName,
                        onConfirm = {
                            // 사용자가 승인함
                            VPAuthManager.sendResponse(requestId, true)
                            finish()
                        },
                        onDismiss = {
                            // 사용자가 거부함
                            VPAuthManager.sendResponse(requestId, false)
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // 뒤로가기 = 거부
        val requestId = intent.getStringExtra("request_id") ?: ""
        VPAuthManager.sendResponse(requestId, false)
        super.onBackPressed()
    }
}