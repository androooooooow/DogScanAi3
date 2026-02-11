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

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(token: String, user: User) {
        with(sharedPreferences.edit()) {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            apply()
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun getUser(): User? {
        val id = sharedPreferences.getString(KEY_USER_ID, null)
        val name = sharedPreferences.getString(KEY_USER_NAME, null)
        val email = sharedPreferences.getString(KEY_USER_EMAIL, null)

        return if (id != null && name != null && email != null) {
            User(id, name, email)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun logout() {
        with(sharedPreferences.edit()) {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            apply()
        }
    }

    fun getBearerToken(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
}