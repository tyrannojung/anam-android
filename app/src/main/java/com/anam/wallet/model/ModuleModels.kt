package com.anam.wallet.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Data model for module information
 */
data class ModuleInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val version: String
) {
    companion object {
        fun fromJson(json: String): ModuleInfo {
            val obj = JSONObject(json)
            return ModuleInfo(
                id = obj.optString("id", ""),
                name = obj.optString("name", ""),
                symbol = obj.optString("symbol", ""),
                iconUrl = obj.optString("iconUrl", ""),
                version = obj.optString("version", "")
            )
        }
    }
}

/**
 * Data model for account information
 */
data class AccountInfo(
    val address: String,
    val name: String
) {
    companion object {
        fun fromJsonArray(json: String): List<AccountInfo> {
            val accounts = mutableListOf<AccountInfo>()
            val array = JSONArray(json)
            
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                accounts.add(
                    AccountInfo(
                        address = obj.optString("address", ""),
                        name = obj.optString("name", "")
                    )
                )
            }
            
            return accounts
        }
    }
}

/**
 * Data model for balance summary
 */
data class BalanceSummary(
    val address: String,
    val balance: String,
    val fiatValue: String,
    val fiatCurrency: String = "USD"
) {
    companion object {
        fun fromJson(json: String): BalanceSummary {
            val obj = JSONObject(json)
            return BalanceSummary(
                address = obj.optString("address", ""),
                balance = obj.optString("balance", "0"),
                fiatValue = obj.optString("fiatValue", "0"),
                fiatCurrency = obj.optString("fiatCurrency", "USD")
            )
        }
    }
}

/**
 * Data model for network information
 */
data class NetworkInfo(
    val name: String,
    val chainId: String,
    val isMainnet: Boolean
) {
    companion object {
        fun fromJson(json: String): NetworkInfo {
            val obj = JSONObject(json)
            return NetworkInfo(
                name = obj.optString("name", "Unknown"),
                chainId = obj.optString("chainId", ""),
                isMainnet = obj.optBoolean("isMainnet", false)
            )
        }
    }
}

/**
 * Data model for token information
 */
data class TokenInfo(
    val symbol: String,
    val name: String,
    val iconUrl: String,
    val contractAddress: String
) {
    companion object {
        fun fromJsonArray(json: String): List<TokenInfo> {
            val tokens = mutableListOf<TokenInfo>()
            val array = JSONArray(json)
            
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                tokens.add(
                    TokenInfo(
                        symbol = obj.optString("symbol", ""),
                        name = obj.optString("name", ""),
                        iconUrl = obj.optString("iconUrl", ""),
                        contractAddress = obj.optString("contractAddress", "")
                    )
                )
            }
            
            return tokens
        }
    }
}