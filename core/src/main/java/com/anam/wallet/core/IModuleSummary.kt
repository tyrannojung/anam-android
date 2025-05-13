package com.anam.wallet.core

/**
 * Interface for providing module summary information
 */
interface IModuleSummary {
    /**
     * Returns basic module information
     * Includes: module name, symbol, icon URL, version
     */
    fun getModuleInfo(): String

    /**
     * Returns a list of account information
     * Includes: account addresses, account names
     */
    fun getAccounts(): String

    /**
     * Returns balance summary for a specific address
     * Includes: address, balance, fiat value
     */
    fun getBalanceSummary(address: String): String

    /**
     * Returns information about the currently connected network
     * Includes: network name (Mainnet, Testnet, etc.), chain ID
     */
    fun getNetworkInfo(): String

    /**
     * Returns a list of supported tokens
     * Includes: token symbol, name, icon URL, contract address
     */
    fun getSupportedTokens(): String
}