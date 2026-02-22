package network.model

import android.content.Context
import android.content.SharedPreferences

object AuthManager {

    private lateinit var sharedPreferences: SharedPreferences
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    /**
     * Tinatawag ito sa DogScanApplication para i-setup ang SharedPreferences.
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * I-save ang login session data.
     */
    fun saveUser(token: String, user: User) {
        if (!::sharedPreferences.isInitialized) return

        with(sharedPreferences.edit()) {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_EMAIL, user.email)
            apply()
        }
    }

    /**
     * Kinukuha ang token. May safety check para iwas 'UninitializedPropertyAccessException'.
     */
    fun getToken(): String? {
        if (!::sharedPreferences.isInitialized) {
            return null
        }
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    /**
     * Kinukuha ang kasalukuyang User object mula sa local storage.
     */
    fun getUser(): User? {
        if (!::sharedPreferences.isInitialized) return null

        val id = sharedPreferences.getString(KEY_USER_ID, null)
        val username = sharedPreferences.getString(KEY_USER_NAME, null)
        val email = sharedPreferences.getString(KEY_USER_EMAIL, null)

        return if (id != null && username != null && email != null) {
            User(id, username, email)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * Nililinis ang lahat ng data sa logout.
     */
    fun logout() {
        if (!::sharedPreferences.isInitialized) return

        with(sharedPreferences.edit()) {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            apply()
        }
    }

    /**
     * Shortcut para sa Authorization header format.
     */
    fun getBearerToken(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
}