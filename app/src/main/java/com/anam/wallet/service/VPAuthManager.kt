package com.anam.wallet.service

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import java.util.UUID

/**
 * VP 인증 요청 관리자
 * 프로세스 간 비동기 통신을 동기적으로 처리
 */
object VPAuthManager {
    private val responseChannels = mutableMapOf<String, Channel<Boolean>>()
    
    /**
     * 인증 요청 시작
     */
    suspend fun requestAuth(
        context: Context,
        requesterName: String,
        challenge: String,
        presentationDefinition: String
    ): Boolean {
        val requestId = UUID.randomUUID().toString()
        val responseChannel = Channel<Boolean>(1)
        responseChannels[requestId] = responseChannel
        
        try {
            // AuthRequestActivity 시작
            val intent = Intent(context, AuthRequestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("request_id", requestId)
                putExtra(AuthRequestActivity.EXTRA_REQUESTER_NAME, requesterName)
                putExtra(AuthRequestActivity.EXTRA_CHALLENGE, challenge)
                putExtra(AuthRequestActivity.EXTRA_PRESENTATION_DEFINITION, presentationDefinition)
            }
            context.startActivity(intent)
            
            // 응답 대기 (30초 타임아웃)
            return withTimeout(30_000) {
                responseChannel.receive()
            }
        } catch (e: Exception) {
            return false
        } finally {
            responseChannels.remove(requestId)
        }
    }
    
    /**
     * AuthRequestActivity에서 결과 전달
     */
    fun sendResponse(requestId: String, approved: Boolean) {
        responseChannels[requestId]?.trySend(approved)
    }
}