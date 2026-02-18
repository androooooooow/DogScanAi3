package com.firstapp.dogscanai.models

data class DogScannerResponse(
    val success: Boolean,
    val breed: String?,
    val confidence: Double?,
    val age: AgeResult?,
    val emotion: String?,
    val error: String?
)

data class AgeResult(
    val age: String,
    val confidence: Double
)