package network.api

import com.dogscanai.models.AuthResponse
import com.dogscanai.models.RegisterRequest
import network.model.ApiResponse
import network.model.LoginRequest
import network.model.ProfileResponse
import network.model.ScanCountResponse
import network.model.UpdateProfileRequest
import network.model.UpdateProfileResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    @POST("api/auth/update-profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>



    @GET("api/scan-count/{email}")
    suspend fun getScanCount(
        @Path("email") email: String,
        @Header("Authorization") token: String
    ): Response<ScanCountResponse>
}