package network.api

import com.dogscanai.models.AuthResponse
import com.dogscanai.models.RegisterRequest
import network.model.ApiResponse
import network.model.BreedDetailResponse
import network.model.BreedResponse
import network.model.LoginRequest
import network.model.ProfileResponse
import network.model.SaveScanRequest
import network.model.SaveScanResponse
import network.model.ScanCountResponse
import network.model.ScanHistoryResponse
import network.model.UpdateUsernameRequest
import network.model.UpdateEmailRequest
import network.model.UpdatePasswordRequest
import network.model.UploadImageResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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

    @PUT("api/profile/username")
    fun updateUsername(
        @Header("Authorization") token: String,
        @Body request: UpdateUsernameRequest
    ): Call<ResponseBody>

    @PUT("api/profile/email")
    fun updateEmail(
        @Header("Authorization") token: String,
        @Body request: UpdateEmailRequest
    ): Call<ResponseBody>

    @PUT("api/profile/password")
    fun updatePassword(
        @Header("Authorization") token: String,
        @Body request: UpdatePasswordRequest
    ): Call<ResponseBody>

    @GET("api/scan-count/{email}")
    suspend fun getScanCount(
        @Path("email") email: String,
        @Header("Authorization") token: String
    ): Response<ScanCountResponse>

    @GET("api/breeds")
    suspend fun getBreeds(
        @Header("Authorization") token: String
    ): Response<List<BreedResponse>>

    @Multipart
    @POST("api/upload")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Call<UploadImageResponse>

    @POST("api/scans")
    fun saveScan(
        @Header("Authorization") token: String,
        @Body request: SaveScanRequest
    ): Call<SaveScanResponse>

    @GET("api/scans")
    fun getScanHistory(
        @Header("Authorization") token: String
    ): Call<List<ScanHistoryResponse>>

    // ✅ NEW - Get breed details from DB by breedId
    @GET("api/scans/breed/{breedId}")
    fun getBreedDetail(
        @Header("Authorization") token: String,
        @Path("breedId") breedId: Int
    ): Call<BreedDetailResponse>

    // ✅ NEW - Delete scan by id
    @DELETE("api/scans/{id}")
    fun deleteScan(
        @Header("Authorization") token: String,
        @Path("id") scanId: Int
    ): Call<ResponseBody>

    // ✅ NEW - Get public scan usage (no auth needed)
    @GET("api/scans/public/usage")
    fun getPublicScanUsage(): Call<ResponseBody>
}