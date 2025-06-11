package com.anam.wallet.model.identity

data class WalletStatus(
    val isInitialized: Boolean,
    val hasVC: Boolean,
    val hasSecureKey: Boolean,
    val walletInfo: WalletInfo?
)