package network.model

import com.google.gson.annotations.SerializedName

// --- PREDICTION MODELS (Galing sa AI) ---
data class BreedResponse(
    val success: Boolean,
    val predictions: List<BreedResult>?,
    @SerializedName("top_breed") val topBreed: String?,
    @SerializedName("top_confidence") val topConfidence: Double?,
    val error: String?
)

data class BreedResult(
    val breed: String,
    val confidence: Double
)

data class DiseaseResponse(
    val success: Boolean,
    @SerializedName("is_healthy") val isHealthy: Boolean?,
    @SerializedName("top_prediction") val topPrediction: DiseaseDetail?,
    val recommendation: String?,
    val error: String?
)

data class DiseaseDetail(
    val name: String,
    val confidence: Double,
    val description: String,
    val treatment: String,
    val severity: String
)

// --- DATABASE SAVE MODELS (Para sa PostgreSQL endpoints mo) ---
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