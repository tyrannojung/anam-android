package com.anam.wallet.domain.usecase.identity

import com.anam.wallet.data.datasource.local.crypto.SimpleKeyManager
import com.anam.wallet.domain.repository.WalletRepository
import com.anam.wallet.model.identity.WalletInfo
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val keyManager: SimpleKeyManager
) {
    suspend operator fun invoke(): Result<WalletInfo> {
        return try {
            // 1. Generate key pair
            keyManager.generateKeyPair()
            val publicKey = keyManager.getPublicKeyPem()
            
            // 2. Register DID with server
            val result = walletRepository.createWallet(publicKey)
            
            result.onSuccess { walletInfo ->
                // 3. Save wallet info locally
                walletRepository.saveWalletInfo(walletInfo)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}