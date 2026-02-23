package com.firstapp.dogscanai.models

data class DogScannerResponse(
    val scan_type: String?,
    val result_type: String?,
    val top_breeds: List<BreedInfo> = emptyList(),
    val reasons: List<String>? = emptyList(),
    val emotion: EmotionInfo? = null,
    val age: AgeInfo? = null
)

data class BreedInfo(
    val rank: Int?,
    val class_index: Int?,
    val class_name: String?,
    val display_name: String?,
    val breed_id: String?,
    val confidence: Double?,
    val mix_share: Double?
)

data class EmotionInfo(
    val class_index: Int?,
    val class_name: String?,
    val display_name: String?,
    val confidence: Double?
)

data class AgeInfo(
    val class_index: Int?,
    val class_name: String?,
    val display_name: String?,
    val confidence: Double?
)