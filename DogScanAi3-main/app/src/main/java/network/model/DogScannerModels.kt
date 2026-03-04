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

// ✅ FIXED - scan_type is REQUIRED by backend (validates "breed" or "disease")
data class SaveScanRequest(
    @SerializedName("image_url") val image_url: String,
    @SerializedName("predictions") val predictions: List<ScanPrediction>,
    @SerializedName("scan_type") val scan_type: String = "breed"  // ✅ backend requires this
)

// ✅ FIXED - added breed_id (backend stores it), made fields non-nullable with defaults
data class ScanPrediction(
    @SerializedName("rank") val rank: Int,
    @SerializedName("breed_id") val breed_id: Int? = null,        // ✅ backend expects this
    @SerializedName("class_name") val class_name: String = "",
    @SerializedName("display_name") val display_name: String = "",
    @SerializedName("confidence") val confidence: Double = 0.0
)

// ✅ FIXED - backend returns {scan_id, scanned_at} on 201 success
data class SaveScanResponse(
    @SerializedName("scan_id") val scan_id: Int?,
    @SerializedName("scanned_at") val scanned_at: String?
) {
    val success: Boolean
        get() = scan_id != null
}

// ✅ FIXED - matches GET /api/scans response, scan_type now from actual data
data class ScanHistoryResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("scanned_at") val scanned_at: String?,
    @SerializedName("scan_type") val scan_type: String?,           // ✅ backend returns this field
    @SerializedName("predictions") val predictions: List<ScanPredictionItem>?
) {
    val top_prediction: String?
        get() = predictions?.minByOrNull { it.rank ?: 99 }?.display_name

    val confidence: Double?
        get() = predictions?.minByOrNull { it.rank ?: 99 }?.confidence
}

// ✅ unchanged - already correct
data class ScanPredictionItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("rank") val rank: Int?,
    @SerializedName("breed_id") val breed_id: Int?,
    @SerializedName("class_name") val class_name: String?,
    @SerializedName("display_name") val display_name: String?,
    @SerializedName("confidence") val confidence: Double?
)
data class UploadImageResponse(
    @SerializedName("image_url") val image_url: String?  // e.g. "http://192.168.137.1:5000/uploads/abc.jpg"
)data class UpdateUsernameRequest(
    @SerializedName("username") val username: String,
    @SerializedName("current_password") val current_password: String
)

// ✅ For PUT /api/profile/email
data class UpdateEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("current_password") val current_password: String
)

// ✅ For PUT /api/profile/password
data class UpdatePasswordRequest(
    @SerializedName("current_password") val current_password: String,
    @SerializedName("new_password") val new_password: String
)

// ✅ Shared response model for all profile updates
data class UpdateProfileResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("user") val user: User?,
    @SerializedName("requires_relogin") val requires_relogin: Boolean? = false,  // ✅ this is missing
    @SerializedName("error") val error: String? = null
)