package network.model

data class ScanCountResponse(
    val success: Boolean,
    val message: String? = null,
    val total: Int? = null
)