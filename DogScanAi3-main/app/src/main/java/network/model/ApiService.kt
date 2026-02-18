package network.model

import com.dogscanai.models.AuthResponse
import com.dogscanai.models.RegisterRequest
import network.model.ProfileResponse
import network.model.ScanCountResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Import mo rin ang User model mo kung nasaan man ito nakalagay
// import network.model.User

interface ApiService {

    // --- AUTH ENDPOINTS ---

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    // --- PROFILE UPDATE ENDPOINT (ITO ANG DAGDAG) ---
    // Gagamitin ito para ma-update ang name at email sa database server.
    @POST("api/auth/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: User
    ): Response<AuthResponse>

    // --- SCAN ENDPOINT ---

    @GET("api/scan-count/{email}")
    suspend fun getScanCount(
        @Path("email") email: String,
        @Header("Authorization") token: String
    ): Response<ScanCountResponse>
}