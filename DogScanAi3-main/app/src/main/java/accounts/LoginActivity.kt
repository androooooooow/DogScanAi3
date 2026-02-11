package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogscanai.models.ErrorResponse
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.ActivityLoginBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.model.AuthManager  // ADD THIS IMPORT
import network.model.LoginRequest
import network.model.RetrofitClient

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ ADD THIS LINE - INITIALIZE AUTH MANAGER
        AuthManager.initialize(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            goToDashboard()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }

        binding.signuplink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email = email, password = password)
                val response = RetrofitClient.getClient().login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // ✅ ALSO SAVE TO AuthManager (for RetrofitClient token)
                    authResponse.token?.let { token ->
                        authResponse.user?.let { user ->
                            AuthManager.saveUser(token, user)
                        }
                    }

                    // Save session to SessionManager
                    sessionManager.saveSession(authResponse.token, authResponse.user)

                    // Show welcome message
                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome, ${authResponse.user?.name}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to Dashboard
                    goToDashboard()
                } else {
                    // Parse error response
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                            errorResponse.error
                        } catch (e: Exception) {
                            "Login failed. Please try again."
                        }
                    } else {
                        "Login failed. Please try again."
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Network error: Could not reach server. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.loginButton.isEnabled = !show
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}