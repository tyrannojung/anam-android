package com.anam.wallet.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    
    // Change this to your server URL
    private const val BASE_URL = "http://10.0.2.2:8080/" // For Android Emulator
    // private const val BASE_URL = "http://YOUR_SERVER_IP:8080/" // For real device
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val didApiService: DIDApiService by lazy {
        retrofit.create(DIDApiService::class.java)
    }
}