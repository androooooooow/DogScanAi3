package com.firstapp.dogscanai.utils

import android.content.Context
import android.content.SharedPreferences
import network.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
        private const val KEY_FIRST_TIME = "is_first_time"
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun getToken(): String? = sharedPreferences.getString(KEY_TOKEN, null)

    fun saveSession(token: String, user: User) {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) gson.fromJson(userJson, User::class.java) else null
    }

    fun saveUser(user: User) {
        sharedPreferences.edit()
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME, isFirstTime).apply()
    }

    fun isFirstTimeLaunch(): Boolean =
        sharedPreferences.getBoolean(KEY_FIRST_TIME, true)

    fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER)
            .apply()
    }

    fun getBearerToken(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }
}
