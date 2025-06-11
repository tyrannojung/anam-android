package com.anam.wallet.domain.repository

import com.anam.wallet.model.identity.WalletInfo

interface WalletRepository {
    suspend fun getWalletInfo(): WalletInfo?
    suspend fun createWallet(publicKeyPem: String): Result<WalletInfo>
    suspend fun saveWalletInfo(walletInfo: WalletInfo)
    suspend fun clearWallet()
    fun isWalletInitialized(): Boolean
}