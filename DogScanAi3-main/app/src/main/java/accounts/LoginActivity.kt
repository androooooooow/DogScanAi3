package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogscanai.models.ErrorResponse
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.ActivityLoginBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.model.AuthManager
import network.model.LoginRequest
import network.model.RetrofitClient

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AuthManager para sa API connection
        AuthManager.initialize(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Auto-login kung may existing session na
        if (sessionManager.isLoggedIn()) {
            goToDashboard()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // --- BUTTON PARA BUMALIK SA ONBOARDING ---
        binding.btnBackToOnboarding.setOnClickListener {
            // I-reset ang flag para lumabas ang tutorial/onboarding views
            sessionManager.setFirstTimeLaunch(true)

            // Hanapin ang "Launcher" Activity (ang screen na unang bumubukas sa app mo)
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Onboarding screen not found.", Toast.LENGTH_SHORT).show()
            }
        }

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

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email = email, password = password)
                val response = RetrofitClient.getClient().login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // I-save ang user at token
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
                    } catch (e: Exception) {
                        "Invalid email or password"
                    }
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
        binding.loginButton.isEnabled = !show
    }

    private fun goToDashboard() {
        // Palitan ang 'DashboardActivity' sa totoong dashboard filename mo
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}