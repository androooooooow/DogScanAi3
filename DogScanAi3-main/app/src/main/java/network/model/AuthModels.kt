package network.model

// ❌ BURAHIN MO YUNG IMPORT ANDROID SERVICE AUTOFILL DITO KUNG MERON MAN

// Request models
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

// Response models
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: User? = null
)



// Ito ang iyong sariling User model
data class User(
    val id: String,
    val username: String,
    val email: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)

data class UpdateProfileRequest(
    val username: String,
    val email: String,
    val password: String? = null
)

// ✅ FIX: Palitan ang 'UserData' ng 'User' (yung class na nasa itaas)


