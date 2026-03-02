package network.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FlaskClient {
    private const val BASE_URL = "http://192.168.137.1:5001/"

    val instance: DogScannerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogScannerApi::class.java)
    }
}