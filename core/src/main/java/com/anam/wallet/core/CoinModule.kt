package com.anam.wallet.core

abstract class CoinModule : ICoinModule.Stub() {
    // 모든 메서드를 자식 클래스에서 구현하도록 함
    abstract override fun getSymbol(): String
    abstract override fun request(jsonRpcPayload: String): String
    abstract override fun createAccount(encMessage: String): String
}