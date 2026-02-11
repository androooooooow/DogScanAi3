// AuthResponse.kt
package network.model

data class oginResponse(
    val token: String?,         // JWT or session token
    val message: String?        // Success or error message
)
