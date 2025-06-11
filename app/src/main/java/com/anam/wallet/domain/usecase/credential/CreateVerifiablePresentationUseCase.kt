package com.anam.wallet.domain.usecase.credential

import com.anam.wallet.domain.repository.CredentialRepository
import com.anam.wallet.model.identity.VerifiablePresentation
import javax.inject.Inject

class CreateVerifiablePresentationUseCase @Inject constructor(
    private val credentialRepository: CredentialRepository
) {
    suspend operator fun invoke(challenge: String? = null): Result<VerifiablePresentation> {
        return credentialRepository.createVerifiablePresentation(challenge)
    }
}