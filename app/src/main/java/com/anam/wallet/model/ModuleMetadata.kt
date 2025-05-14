package com.anam.wallet.model

import org.json.JSONObject

/**
 * Data model for module metadata from server
 */
data class ModuleMetadata(
    val id: Long,
    val name: String,
    val version: String,
    val packageName: String,
    val implementationClass: String,
    val description: String,
    val fileSize: Long,
    val sha256Hash: String,
    val isActive: Boolean,
    val uploadedAt: String
) {
    companion object {
        fun fromJson(json: String): ModuleMetadata {
            val jsonObject = JSONObject(json)
            return ModuleMetadata(
                id = jsonObject.getLong("id"),
                name = jsonObject.getString("name"),
                version = jsonObject.getString("version"),
                packageName = jsonObject.getString("packageName"),
                implementationClass = jsonObject.getString("implementationClass"),
                description = jsonObject.getString("description"),
                fileSize = jsonObject.getLong("fileSize"),
                sha256Hash = jsonObject.getString("sha256Hash"),
                isActive = jsonObject.getBoolean("isActive"),
                uploadedAt = jsonObject.getString("uploadedAt")
            )
        }
    }
}