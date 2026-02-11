package com.dogscanai.models

import network.model.User

data class AuthResponse(
    val success: Boolean? = null,  // ADD THIS
    val message: String? = null,   // MAKE NULLABLE
    val token: String,            // KEEP AS NON-NULL
    val user: User                // KEEP AS NON-NULL
)