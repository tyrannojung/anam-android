package com.anam.wallet.domain.repository

import com.anam.wallet.model.identity.VerifiableCredential
import com.anam.wallet.model.identity.WalletInfo

interface StorageRepository {
    /**
     * Wallet storage operations
     */
    suspend fun saveWalletInfo(walletInfo: WalletInfo): Result<Unit>
    suspend fun getWalletInfo(): Result<WalletInfo?>
    suspend fun isWalletInitialized(): Boolean
    suspend fun clearWallet(): Result<Unit>
    
    /**
     * VC storage operations
     */
    suspend fun saveVC(vc: VerifiableCredential): Result<Unit>
    suspend fun getVC(): Result<VerifiableCredential?>
    suspend fun hasVC(): Boolean
    suspend fun deleteVC(): Result<Unit>
}