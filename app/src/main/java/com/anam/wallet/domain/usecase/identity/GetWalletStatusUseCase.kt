package com.anam.wallet.domain.usecase.identity

import com.anam.wallet.domain.repository.IdentityRepository
import com.anam.wallet.model.identity.WalletStatus
import javax.inject.Inject

class GetWalletStatusUseCase @Inject constructor(
    private val identityRepository: IdentityRepository
) {
    suspend operator fun invoke(): WalletStatus {
        return identityRepository.getWalletStatus()
    }
}