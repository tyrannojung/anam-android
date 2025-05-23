package com.anam.wallet.model

data class DidLicense(
    val id: String,
    val licenseNumber: String,
    val holder: String,
    val controller: String,
    val status: String,
    val licenseType: String,
    val created: String,
    val updated: String
)