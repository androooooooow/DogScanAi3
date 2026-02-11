// AuthRequest.kt
package network.model

data class LoginRequest(
    val email: String,
    val password: String,
    val username: String? = null // username is optional for login
)
