package com.firstapp.dogscanai.network // CORRECTED: Package now matches file location and project structure

import network.model.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // --- IMPORTANT ---
    // This is the ONLY part you need to worry about.
    // Use http://10.0.2.2:5000/ for the Android Emulator.
    // Use http://YOUR_COMPUTER_IP:5000/ for a physical phone on the same Wi-Fi.
    private const val BASE_URL = "http://10.40.20.15:5000/"

    // Create a logger to see API requests and responses in Logcat (for debugging)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Create a custom HTTP client with the logger
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Create the single Retrofit instance using a lazy delegate
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Publicly expose the fully configured ApiService implementation
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
