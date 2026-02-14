package network.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 192.168.137.1 ang default IP ng Windows Mobile Hotspot
    private const val BASE_URL = "http://192.168.137.1:5000/"

    val instance: DogScannerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogScannerApi::class.java)
    }
}