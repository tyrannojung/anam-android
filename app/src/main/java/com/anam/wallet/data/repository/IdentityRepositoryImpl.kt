package com.anam.wallet.data.repository

import com.anam.wallet.data.datasource.local.crypto.SimpleKeyManager
import com.anam.wallet.domain.repository.IdentityRepository
import com.anam.wallet.model.identity.RegisterDIDRequest
import com.anam.wallet.model.identity.WalletInfo
import com.anam.wallet.data.datasource.remote.DIDApiService
import com.anam.wallet.model.identity.WalletStatus
import com.anam.wallet.data.datasource.local.storage.VCManager
import com.anam.wallet.data.datasource.local.storage.WalletManager
import javax.inject.Inject

class IdentityRepositoryImpl @Inject constructor(
    private val keyManager: SimpleKeyManager,
    private val walletManager: WalletManager,
    private val vcManager: VCManager,
    private val apiService: DIDApiService
) : IdentityRepository {
    
    override suspend fun initializeWallet(userName: String): Result<WalletInfo> {
        return try {
            // 1. Generate key pair
            val (publicKeyPem, _) = keyManager.generateAndStoreKeyPair("user_key")
            val publicKeyBase64 = keyManager.pemToBase64(publicKeyPem)
            
            // 2. Register DID on server
            val request = RegisterDIDRequest(
                publicKeyPem = publicKeyBase64,
                meta = mapOf("name" to userName)
            )
            
            val response = apiService.registerUserDID(request)
            if (!response.isSuccessful) {
                return Result.failure(Exception("DID registration failed: ${response.message()}"))
            }
            
            val didResponse = response.body()!!
            
            // 3. Save wallet info
            walletManager.saveWalletInfo(
                userId = didResponse.userId,
                userDid = didResponse.did,
                publicKey = publicKeyPem
            )
            
            val walletInfo = WalletInfo(
                userId = didResponse.userId,
                userDid = didResponse.did,
                publicKey = publicKeyPem
            )
            
            Result.success(walletInfo)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWalletStatus(): WalletStatus {
        val hasWallet = walletManager.isWalletInitialized()
        val hasVC = vcManager.hasVC()
        val hasKey = keyManager.hasKey("user_key")
        
        return WalletStatus(
            isInitialized = hasWallet,
            hasVC = hasVC,
            hasSecureKey = hasKey,
            walletInfo = if (hasWallet) walletManager.getWalletInfo() else null
        )
    }
    
    override suspend fun getWalletInfo(): WalletInfo? {
        return walletManager.getWalletInfo()
    }
    
    override suspend fun isWalletInitialized(): Boolean {
        return walletManager.isWalletInitialized()
    }
}