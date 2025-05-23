package com.anam.wallet.crypto

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.ECGenParameterSpec

class SimpleKeyManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "secure_keys"
        private const val KEY_PRIVATE_KEY = "private_key"
        private const val EC_CURVE = "secp256r1"
        private const val EC_ALGORITHM = "EC"
        
        init {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    private val encryptedPrefs by lazy {
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
     * Generate EC key pair and store securely
     */
    fun generateAndStoreKeyPair(alias: String): Pair<String, String> {
        // Generate EC key pair using BouncyCastle
        val keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM, BouncyCastleProvider())
        keyPairGenerator.initialize(ECGenParameterSpec(EC_CURVE))
        val keyPair = keyPairGenerator.generateKeyPair()
        
        // Convert to PEM format
        val publicKeyPem = publicKeyToPem(keyPair.public)
        val privateKeyPem = privateKeyToPem(keyPair.private)
        
        // Store private key securely
        encryptedPrefs.edit()
            .putString("${KEY_PRIVATE_KEY}_$alias", privateKeyPem)
            .apply()
        
        return Pair(publicKeyPem, privateKeyPem)
    }
    
    /**
     * Sign data with stored private key
     */
    fun signData(data: ByteArray, alias: String): String? {
        return try {
            val privateKeyPem = encryptedPrefs.getString("${KEY_PRIVATE_KEY}_$alias", null) ?: return null
            val privateKey = pemToPrivateKey(privateKeyPem)
            
            val signature = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider())
            signature.initSign(privateKey)
            signature.update(data)
            Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if key exists
     */
    fun hasKey(alias: String): Boolean {
        return encryptedPrefs.contains("${KEY_PRIVATE_KEY}_$alias")
    }
    
    /**
     * Delete stored key
     */
    fun deleteKey(alias: String): Boolean {
        return try {
            encryptedPrefs.edit()
                .remove("${KEY_PRIVATE_KEY}_$alias")
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun publicKeyToPem(publicKey: PublicKey): String {
        val encoded = publicKey.encoded
        val base64 = Base64.encodeToString(encoded, Base64.NO_WRAP)
        return "-----BEGIN PUBLIC KEY-----\n" +
                base64.chunked(64).joinToString("\n") +
                "\n-----END PUBLIC KEY-----"
    }
    
    private fun privateKeyToPem(privateKey: PrivateKey): String {
        val encoded = privateKey.encoded
        val base64 = Base64.encodeToString(encoded, Base64.NO_WRAP)
        return "-----BEGIN EC PRIVATE KEY-----\n" +
                base64.chunked(64).joinToString("\n") +
                "\n-----END EC PRIVATE KEY-----"
    }
    
    private fun pemToPrivateKey(pemString: String): PrivateKey {
        val keyBytes = Base64.decode(
            pemString
                .replace("-----BEGIN EC PRIVATE KEY-----", "")
                .replace("-----END EC PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), ""),
            Base64.DEFAULT
        )
        
        val keyFactory = KeyFactory.getInstance(EC_ALGORITHM, BouncyCastleProvider())
        val keySpec = org.bouncycastle.asn1.pkcs.PrivateKeyInfo.getInstance(keyBytes)
        val privKeySpec = org.bouncycastle.jce.spec.ECPrivateKeySpec(
            org.bouncycastle.asn1.sec.ECPrivateKey.getInstance(keySpec.parsePrivateKey()).key,
            org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec(EC_CURVE)
        )
        
        return keyFactory.generatePrivate(privKeySpec)
    }
    
    fun pemToBase64(pem: String): String {
        return pem.replace("-----[^-]+-----".toRegex(), "")
            .replace("\\s+".toRegex(), "")
    }
}