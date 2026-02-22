package com.dogscanai.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)
