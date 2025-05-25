package com.anam.wallet.service

import android.content.Context
import com.anam.wallet.crypto.SimpleKeyManager
import com.anam.wallet.crypto.VPManager
import com.anam.wallet.model.*
import com.anam.wallet.network.ApiClient
import com.anam.wallet.storage.VCManager
import com.anam.wallet.storage.WalletManager

class DIDService(private val context: Context) {
    
    private val secureKeyManager = SimpleKeyManager(context)
    private val walletManager = WalletManager(context)
    private val vcManager = VCManager(context)
    private val vpManager = VPManager(secureKeyManager)
    private val apiService = ApiClient.didApiService
    
    /**
     * Initialize wallet - generate keys and register DID
     */
    suspend fun initializeWallet(userName: String = "사용자"): Result<WalletInfo> {
        return try {
            // 1. Generate key pair
            val (publicKeyPem, _) = secureKeyManager.generateAndStoreKeyPair("user_key")
            val publicKeyBase64 = secureKeyManager.pemToBase64(publicKeyPem)
            
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
    
    /**
     * Issue driver license (License DID + VC in one call)
     */
    suspend fun issueDriverLicense(licenseNumber: String = "A-123-456-7890"): Result<VerifiableCredential> {
        return try {
            val walletInfo = walletManager.getWalletInfo()
                ?: return Result.failure(Exception("Wallet not initialized"))
            
            // Create license DID and get VC in one call
            val licenseRequest = LicenseRequest(
                userDid = walletInfo.userDid,
                licenseNumber = licenseNumber
            )
            
            val licenseResponse = apiService.createLicense(licenseRequest)
            if (!licenseResponse.isSuccessful) {
                return Result.failure(Exception("License creation failed: ${licenseResponse.message()}"))
            }
            
            val license = licenseResponse.body()!!
            val vc = license.vc
            
            // Save VC locally
            vcManager.saveVC(vc)
            
            Result.success(vc)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create VP for identity verification
     */
    fun createVerifiablePresentation(challenge: String? = null): Result<VerifiablePresentation> {
        return try {
            val walletInfo = walletManager.getWalletInfo()
                ?: return Result.failure(Exception("Wallet not initialized"))
            
            val vc = vcManager.getVC()
                ?: return Result.failure(Exception("No VC found"))
            
            val actualChallenge = challenge ?: vpManager.generateChallenge()
            
            val vp = vpManager.createVP(vc, walletInfo.userDid, actualChallenge)
                ?: return Result.failure(Exception("VP creation failed"))
            
            Result.success(vp)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify VP with server
     */
    suspend fun verifyVP(vp: VerifiablePresentation, challenge: String): Result<VPVerifyResponse> {
        return try {
            val request = VPVerifyRequest(vp = vp, challenge = challenge)
            val response = apiService.verifyVP(request)
            
            if (!response.isSuccessful) {
                return Result.failure(Exception("VP verification failed: ${response.message()}"))
            }
            
            Result.success(response.body()!!)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get wallet status
     */
    fun getWalletStatus(): WalletStatus {
        val hasWallet = walletManager.isWalletInitialized()
        val hasVC = vcManager.hasVC()
        val hasKey = secureKeyManager.hasKey("user_key")
        
        return WalletStatus(
            isInitialized = hasWallet,
            hasVC = hasVC,
            hasSecureKey = hasKey,
            walletInfo = if (hasWallet) walletManager.getWalletInfo() else null
        )
    }
}

data class WalletStatus(
    val isInitialized: Boolean,
    val hasVC: Boolean,
    val hasSecureKey: Boolean,
    val walletInfo: WalletInfo?
)