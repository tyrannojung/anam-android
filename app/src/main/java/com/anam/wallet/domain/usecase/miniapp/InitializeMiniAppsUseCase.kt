package com.anam.wallet.domain.usecase.miniapp

import com.anam.wallet.domain.repository.MiniAppRepository
import javax.inject.Inject

class InitializeMiniAppsUseCase @Inject constructor(
    private val miniAppRepository: MiniAppRepository
) {
    suspend operator fun invoke(onProgress: (Int, Int) -> Unit) {
        miniAppRepository.initializeBuiltInApps(onProgress)
    }
}