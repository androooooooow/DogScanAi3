package network.api

import network.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DogScannerApi {

    // 1. AI Prediction Endpoints (Multipart for Images)
    @Multipart
    @POST("/predict/breed")
    suspend fun predictBreed(
        @Part image: MultipartBody.Part,
        @Part("user_email") email: RequestBody,
        @Part("user_id") userId: RequestBody?
    ): Response<BreedResponse>

    @Multipart
    @POST("/predict/disease")
    suspend fun predictDisease(
        @Part image: MultipartBody.Part,
        @Part("user_email") email: RequestBody,
        @Part("user_id") userId: RequestBody?
    ): Response<DiseaseResponse>

    // 2. Database Saving Endpoints (JSON post)
    @POST("/save-breed-scan")
    suspend fun saveBreedToDb(
        @Body request: SaveBreedRequest
    ): Response<SaveResponse>

    @POST("/save-disease-scan")
    suspend fun saveDiseaseToDb(
        @Body request: SaveDiseaseRequest
    ): Response<SaveResponse>
}