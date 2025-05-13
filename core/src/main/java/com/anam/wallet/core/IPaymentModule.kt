package com.anam.wallet.core

/**
 * Common interface for all payment modules
 * This will be implemented by external APK modules
 * Extends IModuleSummary to provide additional information about the module
 */
interface IPaymentModule : IModuleSummary {
    /**
     * Get module name
     */
    fun getName(): String

    /**
     * Get symbol (e.g. ETH, BTC)
     */
    fun getSymbol(): String

    /**
     * Send JSON-RPC request to the blockchain
     */
    fun request(jsonRpcPayload: String): String

    /**
     * Create a new account
     */
    fun createAccount(encMessage: String): String
}