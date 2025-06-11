package com.anam.wallet.domain.usecase.miniapp

import com.anam.wallet.domain.repository.MiniAppRepository
import com.anam.wallet.model.miniapp.MiniAppManifest
import javax.inject.Inject

class LoadMiniAppUseCase @Inject constructor(
    private val miniAppRepository: MiniAppRepository
) {
    suspend operator fun invoke(appId: String): Result<MiniAppManifest> {
        return try {
            val manifest = miniAppRepository.loadMiniApp(appId)
            if (manifest != null) {
                Result.success(manifest)
            } else {
                Result.failure(Exception("Failed to load mini app: $appId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}