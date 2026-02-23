package network.api

import com.firstapp.dogscanai.models.DogScannerResponse
import network.model.UpdateProfileRequest
import network.model.UpdateProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DogScannerApi {
    @POST("predict/breed")
    fun scanDog(@Body request: Map<String, String>): Call<DogScannerResponse>


    @POST("api/auth/update-profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>

}

