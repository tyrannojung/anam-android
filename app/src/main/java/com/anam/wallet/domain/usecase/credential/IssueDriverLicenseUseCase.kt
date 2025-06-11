package com.anam.wallet.domain.usecase.credential

import com.anam.wallet.domain.repository.CredentialRepository
import com.anam.wallet.model.identity.VerifiableCredential
import javax.inject.Inject

class IssueDriverLicenseUseCase @Inject constructor(
    private val credentialRepository: CredentialRepository
) {
    suspend operator fun invoke(licenseNumber: String = "A-123-456-7890"): Result<VerifiableCredential> {
        return credentialRepository.issueDriverLicense(licenseNumber)
    }
}