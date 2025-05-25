package com.anam.wallet.model

import com.google.gson.annotations.SerializedName

// DID API Request/Response Models
data class RegisterDIDRequest(
    @SerializedName("publicKeyPem") val publicKeyPem: String,
    @SerializedName("meta") val meta: Map<String, String> = emptyMap()
)

data class RegisterDIDResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("did") val did: String
)

data class LicenseRequest(
    @SerializedName("userDid") val userDid: String,
    @SerializedName("licenseNumber") val licenseNumber: String = "A-123-456-7890"
)

data class LicenseResponse(
    @SerializedName("licenseDid") val licenseDid: String,
    @SerializedName("vc") val vc: VerifiableCredential,
    @SerializedName("storedAt") val storedAt: String? = null,
    @SerializedName("userDid") val userDid: String,
    @SerializedName("licenseNumber") val licenseNumber: String
)

data class VCIssueRequest(
    @SerializedName("licenseDid") val licenseDid: String
)

data class VPVerifyRequest(
    @SerializedName("vp") val vp: VerifiablePresentation,
    @SerializedName("challenge") val challenge: String
)

data class VPVerifyResponse(
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("reason") val reason: String
)

// VC/VP Models
data class VerifiableCredential(
    @SerializedName("@context") val context: List<String>,
    @SerializedName("type") val type: List<String>,
    @SerializedName("issuer") val issuer: Issuer,
    @SerializedName("issuanceDate") val issuanceDate: String,
    @SerializedName("credentialSubject") val credentialSubject: CredentialSubject,
    @SerializedName("proof") val proof: Proof,
    @SerializedName("id") val id: String
)

data class VerifiablePresentation(
    @SerializedName("@context") val context: List<String>,
    @SerializedName("type") val type: List<String>,
    @SerializedName("holder") val holder: String,
    @SerializedName("verifiableCredential") val verifiableCredential: VerifiableCredential,
    @SerializedName("proof") var proof: Proof? = null
)

data class Issuer(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class CredentialSubject(
    @SerializedName("licenseId") val licenseId: String
)

data class Proof(
    @SerializedName("type") val type: String,
    @SerializedName("created") val created: String,
    @SerializedName("proofPurpose") val proofPurpose: String,
    @SerializedName("verificationMethod") val verificationMethod: String,
    @SerializedName("challenge") val challenge: String,
    @SerializedName("proofValue") val proofValue: String
)

// Wallet Models
data class WalletInfo(
    val userId: String,
    val userDid: String,
    val publicKey: String
)