package com.dogscanai.models

import network.model.User

data class AuthResponse(
    val success: Boolean? = null,  
    val message: String? = null,  
    val token: String,            
    val user: User               
)