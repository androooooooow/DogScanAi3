// SessionManager.kt
package com.dogscanai.utils

import android.content.Context
import android.content.SharedPreferences
import network.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()

    init {
        sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
    }

    // Add this method
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun saveSession(token: String, user: User) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER, gson.toJson(user))
        editor.apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun getAuthHeader(): String? {
        val token = getToken()
        return if (token != null) {
            "Bearer $token"
        } else {
            null
        }
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_TOKEN)
        editor.remove(KEY_USER)
        editor.apply()
    }
}