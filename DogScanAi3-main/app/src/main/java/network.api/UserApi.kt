package network.api

import network.model.UpdateProfileRequest
import network.model.UpdateProfileResponse
import retrofit2.Call
import retrofit2.http.*

interface UserApi {

    @POST("api/user/update-profile") // Adjust this endpoint to match your backend
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>

    // Other API endpoints...
}