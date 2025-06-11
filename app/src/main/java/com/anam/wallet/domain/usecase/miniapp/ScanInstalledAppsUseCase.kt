package com.anam.wallet.domain.usecase.miniapp

import com.anam.wallet.domain.repository.MiniAppRepository
import com.anam.wallet.model.miniapp.ScannedMiniApp
import javax.inject.Inject

class ScanInstalledAppsUseCase @Inject constructor(
    private val miniAppRepository: MiniAppRepository
) {
    suspend operator fun invoke(): Result<List<ScannedMiniApp>> {
        return try {
            val apps = miniAppRepository.getInstalledApps()
            Result.success(apps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}