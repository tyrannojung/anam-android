package com.anam.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AnamWalletApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}