package network.api

import com.dogscanai.models.AuthResponse
import com.dogscanai.models.RegisterRequest
import network.model.ApiResponse
import network.model.BreedDetailResponse
import network.model.BreedResponse
import network.model.ContributeRequest
import network.model.ContributeResponse
import network.model.ContributorStats
import network.model.LeaderboardEntry
import network.model.LoginRequest
import network.model.MessagesResponse
import network.model.ProfileResponse
import network.model.SaveScanRequest
import network.model.SaveScanResponse
import network.model.ScanCountResponse
import network.model.ScanHistoryResponse
import network.model.SendMessageRequest
import network.model.SendMessageResponse
import network.model.ThreadResponse
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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ─────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleAuth(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse<Unit>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    // ─────────────────────────────────────────────────────────
    // PROFILE
    // ─────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────
    // BREEDS & SCAN COUNT
    // ─────────────────────────────────────────────────────────

    @GET("api/scan-count/{email}")
    suspend fun getScanCount(
        @Path("email") email: String,
        @Header("Authorization") token: String
    ): Response<ScanCountResponse>

    @GET("api/breeds")
    suspend fun getBreeds(
        @Header("Authorization") token: String
    ): Response<List<BreedResponse>>

    // ─────────────────────────────────────────────────────────
    // SCANS
    // ─────────────────────────────────────────────────────────

    @Multipart
    @POST("api/scans/upload")
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

    @GET("api/scans/breed/{breedId}")
    fun getBreedDetail(
        @Header("Authorization") token: String,
        @Path("breedId") breedId: Int
    ): Call<BreedDetailResponse>

    @DELETE("api/scans/{id}")
    fun deleteScan(
        @Header("Authorization") token: String,
        @Path("id") scanId: Int
    ): Call<ResponseBody>

    @GET("api/scans/public/usage")
    fun getPublicScanUsage(): Call<ResponseBody>

    @PATCH("api/scans/{id}/contribute")
    fun contributeScan(
        @Header("Authorization") token: String,
        @Path("id") scanId: Int
    ): Call<SaveScanResponse>

    // ─────────────────────────────────────────────────────────
    // CONTRIBUTIONS & LEADERBOARD
    // ─────────────────────────────────────────────────────────

    @GET("api/contributors/leaderboard")
    fun getLeaderboard(): Call<List<LeaderboardEntry>>

    @GET("api/contributors/my-stats")
    fun getMyContributionStats(
        @Header("Authorization") token: String
    ): Call<ContributorStats>

    @POST("api/contributors")
    fun submitContribution(
        @Header("Authorization") token: String,
        @Body request: ContributeRequest
    ): Call<ContributeResponse>

    // ─────────────────────────────────────────────────────────
    // ASSISTANT / CHAT
    // ─────────────────────────────────────────────────────────

    @POST("api/assistant/threads/general")
    suspend fun createGeneralThread(
        @Header("Authorization") token: String
    ): ThreadResponse

    @POST("api/assistant/threads/scan")
    suspend fun createScanThread(
        @Header("Authorization") token: String,
        @Body body: Map<String, Any>
    ): ThreadResponse

    @GET("api/assistant/threads/{threadId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: Int,
        @Query("limit") limit: Int = 50
    ): MessagesResponse

    @POST("api/assistant/threads/{threadId}/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Path("threadId") threadId: Int,
        @Body body: SendMessageRequest
    ): SendMessageResponse
}