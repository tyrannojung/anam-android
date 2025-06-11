package com.anam.wallet.di

import com.anam.wallet.data.repository.CredentialRepositoryImpl
import com.anam.wallet.data.repository.IdentityRepositoryImpl
import com.anam.wallet.data.repository.MiniAppRepositoryImpl
import com.anam.wallet.data.repository.WalletRepositoryImpl
import com.anam.wallet.domain.repository.CredentialRepository
import com.anam.wallet.domain.repository.IdentityRepository
import com.anam.wallet.domain.repository.MiniAppRepository
import com.anam.wallet.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindMiniAppRepository(
        miniAppRepositoryImpl: MiniAppRepositoryImpl
    ): MiniAppRepository
    
    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository
    
    @Binds
    @Singleton
    abstract fun bindIdentityRepository(
        identityRepositoryImpl: IdentityRepositoryImpl
    ): IdentityRepository
    
    @Binds
    @Singleton
    abstract fun bindCredentialRepository(
        credentialRepositoryImpl: CredentialRepositoryImpl
    ): CredentialRepository
}