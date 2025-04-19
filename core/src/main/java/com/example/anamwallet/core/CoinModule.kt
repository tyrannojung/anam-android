package com.example.anamwallet.core

abstract class CoinModule : ICoinModule.Stub() {
    override fun createAccount(encMessage: String): String =
        """{"address":"0xDEADBEEF..."}"""
}