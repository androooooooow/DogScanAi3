package network.model

import com.google.gson.annotations.SerializedName

// --- PREDICTION MODELS ---

data class BreedResult(
    val breed: String,
    val confidence: Double
)

data class DiseaseResponse(
    val scan_type: String?,
    val top_diseases: List<DiseaseEntry>
)

data class DiseaseEntry(
    val rank: Int?,
    val class_index: Int?,
    val class_name: String?,
    val display_name: String?,
    val confidence: Double?,
    val description: String?,
    val treatment: String?,
    val severity: String?
)

data class DiseaseDetail(
    val name: String,
    val confidence: Double,
    val description: String,
    val treatment: String,
    val severity: String
)

// --- DATABASE SAVE MODELS ---

data class SaveBreedRequest(
    val user_email: String,
    val user_id: String?,
    val image_name: String,
    val predictions: List<BreedResult>
)

data class SaveDiseaseRequest(
    val user_email: String,
    val user_id: String?,
    val image_name: String,
    val top_prediction: DiseaseDetail?,
    val all_predictions: List<DiseaseDetail>?,
    val is_healthy: Boolean,
    val recommendation: String
)

data class SaveResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("scan_id") val scanId: Int?,
    @SerializedName("scan_date") val scanDate: String?
)

data class SaveScanRequest(
    @SerializedName("image_url") val image_url: String,
    @SerializedName("predictions") val predictions: List<ScanPrediction>,
    @SerializedName("scan_type") val scan_type: String = "breed"
)

data class ScanPrediction(
    @SerializedName("rank") val rank: Int,
    @SerializedName("breed_id") val breed_id: Int? = null,
    @SerializedName("class_name") val class_name: String = "",
    @SerializedName("display_name") val display_name: String = "",
    @SerializedName("confidence") val confidence: Double = 0.0
)

data class SaveScanResponse(
    @SerializedName("scan_id") val scan_id: Int?,
    @SerializedName("scanned_at") val scanned_at: String?
) {
    val success: Boolean
        get() = scan_id != null
}

data class ScanHistoryResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("scanned_at") val scanned_at: String?,
    @SerializedName("scan_type") val scan_type: String?,
    @SerializedName("predictions") val predictions: List<ScanPredictionItem>?
) {
    val top_prediction: String?
        get() = predictions?.minByOrNull { it.rank ?: 99 }?.display_name

    val confidence: Double?
        get() = predictions?.minByOrNull { it.rank ?: 99 }?.confidence
}

// ✅ UPDATED - added breed_info
data class ScanPredictionItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("breed_id") val breed_id: Int?,
    @SerializedName("class_name") val class_name: String?,
    @SerializedName("display_name") val display_name: String?,
    @SerializedName("confidence") val confidence: Double?,
    @SerializedName("breed_info") val breed_info: BreedInfo? // ✅ NEW
)

// ✅ NEW - breed_info object from API response
data class BreedInfo(
    @SerializedName("size") val size: String?,
    @SerializedName("origin") val origin: String?,
    @SerializedName("breed_group") val breed_group: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("temperament") val temperament: List<String>?,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("height_min") val height_min: Int?,
    @SerializedName("height_max") val height_max: Int?,
    @SerializedName("weight_min") val weight_min: Int?,
    @SerializedName("weight_max") val weight_max: Int?,
    @SerializedName("lifespan_min") val lifespan_min: Int?,
    @SerializedName("lifespan_max") val lifespan_max: Int?,
    @SerializedName("health_considerations") val health_considerations: String?,
    @SerializedName("key_health_tips") val key_health_tips: String?,
    @SerializedName("snout") val snout: String?,
    @SerializedName("ears") val ears: String?,
    @SerializedName("coat") val coat: String?,
    @SerializedName("tail") val tail: String?
)

data class UploadImageResponse(
    @SerializedName("image_url") val image_url: String?
)

data class UpdateUsernameRequest(
    @SerializedName("username") val username: String,
    @SerializedName("current_password") val current_password: String
)

data class UpdateEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("current_password") val current_password: String
)

data class UpdatePasswordRequest(
    @SerializedName("current_password") val current_password: String,
    @SerializedName("new_password") val new_password: String
)

data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("user") val user: User?,
    @SerializedName("requires_relogin") val requires_relogin: Boolean? = false,
    @SerializedName("error") val error: String? = null
)

// ✅ NEW - Breed detail response from GET /api/scans/breed/:breedId
data class BreedDetailResponse(
    @SerializedName("breed_id") val breed_id: Int?,
    @SerializedName("class_name") val class_name: String?,
    @SerializedName("display_name") val display_name: String?,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("origin") val origin: String?,
    @SerializedName("breed_group") val breed_group: String?,
    @SerializedName("temperament") val temperament: List<String>?,
    @SerializedName("lifespan_min") val lifespan_min: Int?,
    @SerializedName("lifespan_max") val lifespan_max: Int?,
    @SerializedName("height_min") val height_min: Int?,
    @SerializedName("height_max") val height_max: Int?,
    @SerializedName("weight_min") val weight_min: Int?,
    @SerializedName("weight_max") val weight_max: Int?,
    @SerializedName("health_considerations") val health_considerations: String?,
    @SerializedName("key_health_tips") val key_health_tips: String?,
    @SerializedName("snout") val snout: String?,
    @SerializedName("ears") val ears: String?,
    @SerializedName("coat") val coat: String?,
    @SerializedName("tail") val tail: String?,
    @SerializedName("popularity_score") val popularity_score: Int?
) {
    val temperamentText: String?
        get() = temperament?.joinToString(", ")
}