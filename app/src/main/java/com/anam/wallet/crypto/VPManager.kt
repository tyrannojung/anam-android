package com.anam.wallet.crypto

import com.anam.wallet.model.*
import com.google.gson.Gson
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class VPManager(
    private val secureKeyManager: SimpleKeyManager
) {
    
    private val gson = Gson()
    
    /**
     * Create Verifiable Presentation with Android Keystore signing
     */
    fun createVP(
        vc: VerifiableCredential, 
        holderDid: String, 
        challenge: String,
        keyAlias: String = "user_key"
    ): VerifiablePresentation? {
        
        try {
            // 1. Create VP structure (without proof)
            val vp = VerifiablePresentation(
                context = listOf("https://www.w3.org/ns/credentials/v2"),
                type = listOf("VerifiablePresentation"),
                holder = holderDid,
                verifiableCredential = vc
            )
            
            // 2. Create canonical JSON for signing
            val vpForSigning = JSONObject().apply {
                put("@context", vp.context)
                put("type", vp.type)
                put("holder", vp.holder)
                put("verifiableCredential", JSONObject(gson.toJson(vp.verifiableCredential)))
            }
            
            val canonicalJson = vpForSigning.toString()
            
            // 3. Sign with Android Keystore
            val signature = secureKeyManager.signData(
                canonicalJson.toByteArray(), 
                keyAlias
            ) ?: throw Exception("Failed to sign VP with keystore")
            
            // 4. Add proof to VP
            val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            
            vp.proof = Proof(
                type = "Secp256r1Signature2018",
                created = currentTime,
                proofPurpose = "authentication",
                verificationMethod = "$holderDid#keys-1",
                challenge = challenge,
                proofValue = signature
            )
            
            return vp
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Generate random challenge for VP
     */
    fun generateChallenge(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { chars.random() }
            .joinToString("")
    }
}