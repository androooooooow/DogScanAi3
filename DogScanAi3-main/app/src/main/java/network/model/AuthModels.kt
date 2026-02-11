package network.model

// Request models
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)


// Response models
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)