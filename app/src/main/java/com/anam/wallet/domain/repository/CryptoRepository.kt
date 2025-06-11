package com.anam.wallet.domain.repository

interface CryptoRepository {
    /**
     * Generate and store a new key pair
     */
    suspend fun generateKeyPair(alias: String): Result<Pair<String, String>>
    
    /**
     * Sign data with private key
     */
    suspend fun signData(data: ByteArray, alias: String): Result<String>
    
    /**
     * Get public key in PEM format
     */
    suspend fun getPublicKeyPem(alias: String): Result<String>
    
    /**
     * Check if key exists
     */
    suspend fun hasKey(alias: String): Boolean
    
    /**
     * Delete stored key
     */
    suspend fun deleteKey(alias: String): Result<Unit>
}