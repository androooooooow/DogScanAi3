package com.dogscanai.utils

import android.content.Context
import android.content.SharedPreferences
import network.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
        private const val KEY_FIRST_TIME = "is_first_time"
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun getToken(): String? = sharedPreferences.getString(KEY_TOKEN, null)

    fun saveSession(token: String, user: User) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER, gson.toJson(user))
        editor.apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) gson.fromJson(userJson, User::class.java) else null
    }

    fun setFirstTimeLaunch(isFirstTime: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME, isFirstTime).apply()
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME, true)
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_TOKEN)
        editor.remove(KEY_USER)
        editor.apply()
    }
}