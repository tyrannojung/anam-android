package com.anam.wallet.domain.usecase.identity

import com.anam.wallet.domain.repository.IdentityRepository
import com.anam.wallet.model.identity.WalletInfo
import javax.inject.Inject

class InitializeWalletUseCase @Inject constructor(
    private val identityRepository: IdentityRepository
) {
    suspend operator fun invoke(userName: String = "사용자"): Result<WalletInfo> {
        return identityRepository.initializeWallet(userName)
    }
}