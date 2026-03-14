package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogscanai.models.ErrorResponse
import com.firstapp.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.databinding.ActivityLoginBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.model.AuthManager
import network.model.LoginRequest
import network.api.RetrofitClient  // ✅ CHANGED from network.model to network.api

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.initialize(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            goToDashboard()
            return
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBackToOnboarding.setOnClickListener {
            sessionManager.setFirstTimeLaunch(true)
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Onboarding screen not found.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginButton.setOnClickListener { attemptLogin() }

        binding.signuplink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email    = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email = email, password = password)
                val response = RetrofitClient.getClient().login(loginRequest)  // ✅ now resolves

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    authResponse.token?.let { token ->
                        authResponse.user?.let { user ->
                            AuthManager.saveUser(token, user)
                            sessionManager.saveSession(token, user)
                        }
                    }
                    Toast.makeText(this@LoginActivity, "Welcome back!", Toast.LENGTH_SHORT).show()
                    goToDashboard()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).error
                    } catch (e: Exception) { "Invalid email or password" }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Network Error", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled  = !show
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}