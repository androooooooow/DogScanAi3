package network.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanRepository {

    private val apiService = RetrofitClient.getClient()

    suspend fun getScanCount(email: String): Result<ScanCountResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AuthManager.getBearerToken()
                    ?: return@withContext Result.failure(Exception("Not logged in"))

                val response = apiService.getScanCount(email, token)

                if (response.isSuccessful && response.body()?.success == true) {
                    Result.success(response.body()!!)
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to get scan count"
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}