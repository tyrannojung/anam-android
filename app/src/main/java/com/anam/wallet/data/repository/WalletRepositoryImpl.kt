package com.anam.wallet.data.repository

import com.anam.wallet.domain.repository.WalletRepository
import com.anam.wallet.model.identity.RegisterDIDRequest
import com.anam.wallet.model.identity.WalletInfo
import com.anam.wallet.data.datasource.remote.DIDApiService
import com.anam.wallet.data.datasource.local.storage.WalletManager
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val walletManager: WalletManager,
    private val didApiService: DIDApiService
) : WalletRepository {
    
    override suspend fun getWalletInfo(): WalletInfo? {
        return walletManager.getWalletInfo()
    }
    
    override suspend fun createWallet(publicKeyPem: String): Result<WalletInfo> {
        return try {
            val request = RegisterDIDRequest(publicKeyPem = publicKeyPem)
            val response = didApiService.registerUserDID(request)
            
            if (response.isSuccessful && response.body() != null) {
                val didResponse = response.body()!!
                val walletInfo = WalletInfo(
                    userId = didResponse.userId,
                    userDid = didResponse.did,
                    publicKey = publicKeyPem
                )
                Result.success(walletInfo)
            } else {
                Result.failure(Exception("Failed to register DID: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveWalletInfo(walletInfo: WalletInfo) {
        walletManager.saveWalletInfo(walletInfo)
    }
    
    override suspend fun clearWallet() {
        walletManager.clearWallet()
    }
    
    override fun isWalletInitialized(): Boolean {
        return walletManager.isWalletInitialized()
    }
}