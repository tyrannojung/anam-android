package com.anam.wallet.domain.repository

import com.anam.wallet.model.identity.WalletInfo
import com.anam.wallet.model.identity.WalletStatus

interface IdentityRepository {
    /**
     * Initialize wallet - generate keys and register DID
     */
    suspend fun initializeWallet(userName: String = "사용자"): Result<WalletInfo>
    
    /**
     * Get wallet status
     */
    suspend fun getWalletStatus(): WalletStatus
    
    /**
     * Get wallet info
     */
    suspend fun getWalletInfo(): WalletInfo?
    
    /**
     * Check if wallet is initialized
     */
    suspend fun isWalletInitialized(): Boolean
}