package com.anam.wallet.service

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.anam.wallet.IMainAppService
import com.anam.wallet.crypto.SimpleKeyManager
import com.anam.wallet.crypto.VPManager
import com.anam.wallet.storage.VCManager
import com.anam.wallet.storage.WalletManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.gson.Gson
import kotlin.coroutines.resume

class MainAppService : Service() {
    
    companion object {
        private const val TAG = "MainAppService"
    }
    
    private val keyManager by lazy { SimpleKeyManager(this) }
    private val vpManager by lazy { VPManager(keyManager) }
    private val vcManager by lazy { VCManager(this) }
    private val walletManager by lazy { WalletManager(this) }
    
    private val binder = object : IMainAppService.Stub() {
        
        override fun requestVP(challenge: String, presentationDefinition: String, requesterName: String): String? {
            Log.d(TAG, "requestVP called - challenge: $challenge, requester: $requesterName")
            
            return try {
                runBlocking {
                    // 사용자 동의 받기
                    val userApproved = showAuthRequestDialog(challenge, presentationDefinition, requesterName)
                    
                    if (!userApproved) {
                        Log.d(TAG, "User rejected VP request")
                        return@runBlocking null
                    }
                    
                    // Get stored VCs
                    val vc = vcManager.getVC()
                    if (vc == null) {
                        Log.w(TAG, "No VC available")
                        return@runBlocking null
                    }
                    
                    // Get wallet info
                    val holderDid = walletManager.getUserDid() ?: return@runBlocking null
                    
                    // Create VP with the VC
                    val vp = vpManager.createVP(
                        vc = vc,
                        holderDid = holderDid,
                        challenge = challenge,
                        keyAlias = "user_key"
                    )
                    
                    if (vp != null) {
                        Log.d(TAG, "VP created successfully for requester: $requesterName")
                        com.google.gson.Gson().toJson(vp)
                    } else {
                        Log.e(TAG, "Failed to create VP")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating VP", e)
                null
            }
        }
        
        override fun getWalletInfo(): String {
            Log.d(TAG, "getWalletInfo called")
            
            return try {
                val walletInfo = walletManager.getWalletInfo() ?: return "{}"
                val address = walletInfo.userId
                val did = walletInfo.userDid
                
                """
                {
                    "address": "$address",
                    "did": "$did"
                }
                """.trimIndent()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting wallet info", e)
                "{}"
            }
        }
        
        override fun hasPermission(permission: String): Boolean {
            Log.d(TAG, "hasPermission called for: $permission")
            
            // For now, we'll implement a simple permission check
            // In a real implementation, you would have a proper permission system
            return when (permission) {
                "REQUEST_VP" -> true
                "GET_WALLET_INFO" -> true
                "ACCESS_CAMERA" -> false // Example of denied permission
                else -> false
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound")
        return binder
    }
    
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        return super.onUnbind(intent)
    }
    
    /**
     * 사용자 동의를 받기 위한 다이얼로그 표시
     */
    private suspend fun showAuthRequestDialog(
        challenge: String,
        presentationDefinition: String,
        requesterName: String
    ): Boolean {
        return VPAuthManager.requestAuth(
            context = this,
            requesterName = requesterName,
            challenge = challenge,
            presentationDefinition = presentationDefinition
        )
    }
}