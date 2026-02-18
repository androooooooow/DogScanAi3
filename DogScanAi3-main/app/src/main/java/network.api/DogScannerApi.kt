package network.api

import com.firstapp.dogscanai.models.DogScannerResponse
import network.model.UpdateProfileRequest
import network.model.UpdateProfileResponse// Gagawa tayo nito
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface DogScannerApi {
    @Multipart
    @POST("predict/breed")
    fun scanDog(
        @Part image: MultipartBody.Part
    ): Call<DogScannerResponse>

    // ✅ BAGONG DAGDAG: Update Profile Endpoint
    @POST("api/auth/update-profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>
}