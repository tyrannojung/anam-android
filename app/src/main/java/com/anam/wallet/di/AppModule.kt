package com.anam.wallet.di

import android.content.Context
import com.anam.wallet.data.datasource.local.crypto.SimpleKeyManager
import com.anam.wallet.data.datasource.local.crypto.VPManager
import com.anam.wallet.data.datasource.miniapp.MiniAppFileManager
import com.anam.wallet.data.datasource.miniapp.MiniAppInitializer
import com.anam.wallet.data.datasource.miniapp.MiniAppLoader
import com.anam.wallet.presentation.miniapp.MiniAppManager
import com.anam.wallet.data.datasource.miniapp.MiniAppScanner
import com.anam.wallet.data.datasource.local.storage.VCManager
import com.anam.wallet.data.datasource.local.storage.WalletManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
    
    @Provides
    @Singleton
    fun provideSimpleKeyManager(
        @ApplicationContext context: Context
    ): SimpleKeyManager = SimpleKeyManager(context)
    
    @Provides
    @Singleton
    fun provideVPManager(
        keyManager: SimpleKeyManager
    ): VPManager = VPManager(keyManager)
    
    @Provides
    @Singleton
    fun provideWalletManager(
        @ApplicationContext context: Context
    ): WalletManager = WalletManager(context)
    
    @Provides
    @Singleton
    fun provideVCManager(
        @ApplicationContext context: Context
    ): VCManager = VCManager(context)
    
    @Provides
    @Singleton
    fun provideMiniAppFileManager(
        @ApplicationContext context: Context
    ): MiniAppFileManager = MiniAppFileManager(context)
    
    @Provides
    @Singleton
    fun provideMiniAppLoader(
        @ApplicationContext context: Context
    ): MiniAppLoader = MiniAppLoader(context)
    
    @Provides
    @Singleton
    fun provideMiniAppScanner(
        @ApplicationContext context: Context
    ): MiniAppScanner = MiniAppScanner(context)
    
    @Provides
    fun provideMiniAppInitializer(
        @ApplicationContext context: Context
    ): MiniAppInitializer = MiniAppInitializer(context)
    
    @Provides
    @Singleton
    fun provideMiniAppManager(
        @ApplicationContext context: Context
    ): MiniAppManager = MiniAppManager.getInstance(context)
}