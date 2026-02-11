package network.model
// AuthRepository.kt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.dogscanai.models.AuthResponse
import com.dogscanai.models.ProfileResponse
import com.dogscanai.models.RegisterRequest
import network.model.AuthManager
import network.model.RetrofitClient

class AuthRepository {

    private val apiService = RetrofitClient.getClient()

    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(name, email, password)
                val response = apiService.register(request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Registration failed"
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Login failed"
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }



    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthManager.getBearerToken()
                if (token != null) {
                    apiService.logout(token)
                }
                AuthManager.logout()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}