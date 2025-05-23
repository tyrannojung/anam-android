package com.anam.wallet.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.anam.wallet.model.WalletInfo

class WalletManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "user_wallet"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_DID = "userDid" 
        private const val KEY_PUBLIC_KEY = "publicKey"
        private const val KEY_IS_INITIALIZED = "isInitialized"
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Save wallet information (excluding private key)
     */
    fun saveWalletInfo(userId: String, userDid: String, publicKey: String) {
        encryptedPrefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_DID, userDid)
            .putString(KEY_PUBLIC_KEY, publicKey)
            .putBoolean(KEY_IS_INITIALIZED, true)
            .apply()
    }
    
    /**
     * Get wallet information
     */
    fun getWalletInfo(): WalletInfo? {
        if (!isWalletInitialized()) return null
        
        val userId = encryptedPrefs.getString(KEY_USER_ID, null) ?: return null
        val userDid = encryptedPrefs.getString(KEY_USER_DID, null) ?: return null
        val publicKey = encryptedPrefs.getString(KEY_PUBLIC_KEY, null) ?: return null
        
        return WalletInfo(userId, userDid, publicKey)
    }
    
    /**
     * Get user DID
     */
    fun getUserDid(): String? {
        return encryptedPrefs.getString(KEY_USER_DID, null)
    }
    
    /**
     * Get public key
     */
    fun getPublicKey(): String? {
        return encryptedPrefs.getString(KEY_PUBLIC_KEY, null)
    }
    
    /**
     * Check if wallet is initialized
     */
    fun isWalletInitialized(): Boolean {
        return encryptedPrefs.getBoolean(KEY_IS_INITIALIZED, false)
    }
    
    /**
     * Clear all wallet data
     */
    fun clearWallet() {
        encryptedPrefs.edit().clear().apply()
    }
}