package network.model

// ❌ BURAHIN MO YUNG IMPORT ANDROID SERVICE AUTOFILL DITO KUNG MERON MAN

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

// Ito ang iyong sariling User model
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

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val password: String? = null
)

// ✅ FIX: Palitan ang 'UserData' ng 'User' (yung class na nasa itaas)
data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val user: User? // <--- DAPAT 'User' ITO, HINDI 'UserData'
)