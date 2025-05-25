package com.anam.wallet.service

import android.content.Context
import android.util.Log
import com.anam.wallet.core.IMainApp
import com.anam.wallet.model.VerifiablePresentation
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private const val TAG = "SimpleMainAppService"

/**
 * 단순화된 MainAppService - Service 없이 직접 인스턴스로 사용
 * VP(Verifiable Presentation) 생성 기능 제공
 */
class SimpleMainAppService(private val context: Context) : IMainApp {
    
    private val didService = DIDService(context)
    private val gson = Gson()
    private val mainScope = MainScope()
    
    // 팝업 표시를 위한 콜백
    var onAuthRequest: ((String, CompletableDeferred<Boolean>) -> Unit)? = null
    
    override suspend fun requestVP(
        challenge: String, 
        presentationDefinition: String,
        requesterName: String
    ): String? {
        Log.d(TAG, "VP 생성 요청")
        Log.d(TAG, "Challenge: $challenge")
        Log.d(TAG, "Presentation Definition: $presentationDefinition")
        Log.d(TAG, "Requester: $requesterName")
        
        return try {
            // 1. 사용자 인증 팝업 표시
            val userApproved = CompletableDeferred<Boolean>()
            
            // 메인 스레드에서 팝업 표시 요청
            mainScope.launch {
                onAuthRequest?.invoke(requesterName, userApproved) ?: run {
                    Log.e(TAG, "Auth request callback not set")
                    userApproved.complete(false)
                }
            }
            
            // 사용자 응답 대기
            if (!userApproved.await()) {
                Log.d(TAG, "User rejected VP request")
                return null
            }
            
            // 2. VP 생성
            val vpResult = didService.createVerifiablePresentation(challenge)
            if (vpResult.isFailure) {
                Log.e(TAG, "VP creation failed", vpResult.exceptionOrNull())
                return null
            }
            
            val vp = vpResult.getOrNull() ?: return null
            
            // 3. VP를 JSON 문자열로 변환
            gson.toJson(vp)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in requestVP", e)
            null
        }
    }
}