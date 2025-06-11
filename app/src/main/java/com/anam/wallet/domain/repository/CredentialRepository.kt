package com.anam.wallet.domain.repository

import com.anam.wallet.model.identity.VerifiableCredential
import com.anam.wallet.model.identity.VerifiablePresentation
import com.anam.wallet.model.identity.VPVerifyResponse

interface CredentialRepository {
    /**
     * Issue driver license (License DID + VC in one call)
     */
    suspend fun issueDriverLicense(licenseNumber: String = "A-123-456-7890"): Result<VerifiableCredential>
    
    /**
     * Create VP for identity verification
     */
    suspend fun createVerifiablePresentation(challenge: String? = null): Result<VerifiablePresentation>
    
    /**
     * Verify VP with server
     */
    suspend fun verifyVP(vp: VerifiablePresentation, challenge: String): Result<VPVerifyResponse>
    
    /**
     * Get stored VC
     */
    suspend fun getStoredVC(): VerifiableCredential?
    
    /**
     * Save VC
     */
    suspend fun saveVC(vc: VerifiableCredential)
    
    /**
     * Check if has VC
     */
    suspend fun hasVC(): Boolean
}