package network.api

import com.firstapp.dogscanai.models.DogScannerResponse
import network.model.DiseaseResponse
import network.model.SaveScanRequest
import network.model.SaveScanResponse
import network.model.ScanHistoryResponse
import network.model.UpdateProfileRequest
import network.model.UpdateProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DogScannerApi {

    @POST("predict/breed")
    fun scanDog(@Body request: Map<String, String>): Call<DogScannerResponse>

    @POST("predict/disease")
    fun scanDisease(@Body request: Map<String, String>): Call<DiseaseResponse>

    // ✅ FIXED - was "api/scans/save", correct path is "api/scans"
    @POST("api/scans")
    fun saveScan(
        @Header("Authorization") token: String,
        @Body request: SaveScanRequest
    ): Call<SaveScanResponse>

    // ✅ FIXED - was "api/scan-history", correct path is "api/scans"
    @GET("api/scans")
    fun getScanHistory(
        @Header("Authorization") token: String
    ): Call<List<ScanHistoryResponse>>

    @POST("api/auth/update-profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<UpdateProfileResponse>
}