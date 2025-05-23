package com.anam.wallet.network

import com.anam.wallet.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DIDApiService {
    
    @POST("dids/user")
    suspend fun registerUserDID(
        @Body request: RegisterDIDRequest
    ): Response<RegisterDIDResponse>
    
    @POST("licenses")
    suspend fun createLicense(
        @Body request: LicenseRequest
    ): Response<LicenseResponse>
    
    @POST("vcs/issue")
    suspend fun issueVC(
        @Body request: VCIssueRequest
    ): Response<VerifiableCredential>
    
    @POST("vps/verify")
    suspend fun verifyVP(
        @Body request: VPVerifyRequest
    ): Response<VPVerifyResponse>
    
    @GET("dids/{did}")
    suspend fun getDID(
        @Path("did") did: String
    ): Response<Map<String, Any>>
}