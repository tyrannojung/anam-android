package com.anam.wallet.data.repository

import com.anam.wallet.data.datasource.local.crypto.VPManager
import com.anam.wallet.domain.repository.CredentialRepository
import com.anam.wallet.model.identity.*
import com.anam.wallet.data.datasource.remote.DIDApiService
import com.anam.wallet.data.datasource.local.storage.VCManager
import com.anam.wallet.data.datasource.local.storage.WalletManager
import javax.inject.Inject

class CredentialRepositoryImpl @Inject constructor(
    private val vcManager: VCManager,
    private val vpManager: VPManager,
    private val walletManager: WalletManager,
    private val apiService: DIDApiService
) : CredentialRepository {
    
    override suspend fun issueDriverLicense(licenseNumber: String): Result<VerifiableCredential> {
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
    
    override suspend fun createVerifiablePresentation(challenge: String?): Result<VerifiablePresentation> {
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
    
    override suspend fun verifyVP(vp: VerifiablePresentation, challenge: String): Result<VPVerifyResponse> {
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
    
    override suspend fun getStoredVC(): VerifiableCredential? {
        return vcManager.getVC()
    }
    
    override suspend fun saveVC(vc: VerifiableCredential) {
        vcManager.saveVC(vc)
    }
    
    override suspend fun hasVC(): Boolean {
        return vcManager.hasVC()
    }
}