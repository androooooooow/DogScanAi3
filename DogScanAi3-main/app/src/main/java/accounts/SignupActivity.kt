package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogscanai.models.ErrorResponse
import com.dogscanai.models.RegisterRequest
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.ActivitySignupBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.model.AuthManager  // ADD THIS IMPORT
import network.model.RetrofitClient
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ ADD THIS LINE - INITIALIZE AUTH MANAGER
        AuthManager.initialize(this)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            attemptSignup()
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        val username = binding.username.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()

        // Validate input
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.length < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val registerRequest = RegisterRequest(
                    username = username,
                    email = email,
                    password = password
                )
                val response = RetrofitClient.getClient().register(registerRequest)

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

                    // Show success message
                    Toast.makeText(
                        this@SignupActivity,
                        "Account created successfully! Welcome, ${authResponse.user?.username}!",
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
                            "Signup failed. Please try again."
                        }
                    } else {
                        "Signup failed. Please try again."
                    }
                    Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("SignupActivity", "Register failed", e)
                Toast.makeText(
                    this@SignupActivity,
                    "Network error. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.signupButton.isEnabled = !show
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}