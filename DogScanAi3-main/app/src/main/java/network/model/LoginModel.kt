package network.model

// What we send to backend


// What backend sends back (success)
data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)



// What backend sends back (error)
data class ErrorResponse(
    val error: String
)