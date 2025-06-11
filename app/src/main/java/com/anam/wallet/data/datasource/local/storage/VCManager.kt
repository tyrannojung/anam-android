package com.anam.wallet.data.datasource.local.storage

import android.content.Context
import com.anam.wallet.model.identity.VerifiableCredential
import com.google.gson.Gson
import java.io.File

class VCManager(private val context: Context) {
    
    companion object {
        private const val VC_FILE_NAME = "user.vc"
    }
    
    private val gson = Gson()
    
    /**
     * Save VC to internal storage
     */
    fun saveVC(vc: VerifiableCredential) {
        try {
            val file = File(context.filesDir, VC_FILE_NAME)
            val jsonString = gson.toJson(vc)
            file.writeText(jsonString)
        } catch (e: Exception) {
            throw Exception("Failed to save VC: ${e.message}")
        }
    }
    
    /**
     * Get saved VC
     */
    fun getVC(): VerifiableCredential? {
        return try {
            val file = File(context.filesDir, VC_FILE_NAME)
            if (!file.exists()) return null
            
            val jsonString = file.readText()
            gson.fromJson(jsonString, VerifiableCredential::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if VC exists
     */
    fun hasVC(): Boolean {
        val file = File(context.filesDir, VC_FILE_NAME)
        return file.exists()
    }
    
    /**
     * Delete saved VC
     */
    fun deleteVC(): Boolean {
        return try {
            val file = File(context.filesDir, VC_FILE_NAME)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get VC as JSON string
     */
    fun getVCAsJson(): String? {
        return try {
            val file = File(context.filesDir, VC_FILE_NAME)
            if (!file.exists()) return null
            file.readText()
        } catch (e: Exception) {
            null
        }
    }
}