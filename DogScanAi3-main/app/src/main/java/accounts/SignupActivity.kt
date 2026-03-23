package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogscanai.models.ErrorResponse
import com.dogscanai.models.RegisterRequest
import com.firstapp.dogscanai.databinding.ActivitySignupBinding
import com.firstapp.dogscanai.utils.GoogleAuthHelper
import com.firstapp.dogscanai.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.api.RetrofitClient
import network.model.AuthManager
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.initialize(this)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        googleSignInClient = GoogleAuthHelper.getGoogleSignInClient(this)
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

        binding.googleSignInButton.setOnClickListener {
            if (!binding.checkboxAcceptTerms.isChecked) {
                Toast.makeText(this, "Please accept the scanning rules first.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, GoogleAuthHelper.RC_SIGN_IN)
            }
        }

        // Toggle rules visibility
        binding.tvViewRules.setOnClickListener {
            if (binding.layoutRules.visibility == View.GONE) {
                binding.layoutRules.visibility = View.VISIBLE
                binding.tvViewRules.text = "Hide scanning rules ▴"
            } else {
                binding.layoutRules.visibility = View.GONE
                binding.tvViewRules.text = "View scanning rules ▾"
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GoogleAuthHelper.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    sendGoogleTokenToBackend(idToken)
                } else {
                    Toast.makeText(this, "Google Sign-Up failed: token is null", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-Up error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendGoogleTokenToBackend(idToken: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getClient().googleAuth(
                    mapOf(
                        "idToken"        to idToken,
                        "accept_terms"   to "true",
                        "policy_key"     to "registration_scan_policy",
                        "policy_version" to "2026-03-22"
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    authResponse.token?.let { token ->
                        authResponse.user?.let { user ->
                            AuthManager.saveUser(token, user)
                            sessionManager.saveSession(token, user)
                        }
                    }
                    Toast.makeText(
                        this@SignupActivity,
                        "Account created! Welcome, ${authResponse.user?.username}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToDashboard()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SignupActivity", "Google error: $errorBody")
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).error
                    } catch (e: Exception) { "Google Sign-Up failed." }
                    Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignupActivity, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun attemptSignup() {
        val username        = binding.username.text.toString().trim()
        val email           = binding.email.text.toString().trim()
        val password        = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()
        val acceptedTerms   = binding.checkboxAcceptTerms.isChecked

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
        if (!acceptedTerms) {
            Toast.makeText(this, "Please accept the scanning rules to continue.", Toast.LENGTH_LONG).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val registerRequest = RegisterRequest(
                    username       = username,
                    email          = email,
                    password       = password,
                    accept_terms   = true,
                    policy_key     = "registration_scan_policy",
                    policy_version = "2026-03-22"
                )
                val response = RetrofitClient.getClient().register(registerRequest)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    authResponse.token?.let { token ->
                        authResponse.user?.let { user ->
                            AuthManager.saveUser(token, user)
                            sessionManager.saveSession(token, user)
                        }
                    }
                    Toast.makeText(
                        this@SignupActivity,
                        "Account created successfully! Welcome, ${authResponse.user?.username}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToDashboard()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            Gson().fromJson(errorBody, ErrorResponse::class.java).error
                        } catch (e: Exception) { "Signup failed. Please try again." }
                    } else { "Signup failed. Please try again." }
                    Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
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
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.signupButton.isEnabled = !show
        binding.googleSignInButton.isEnabled = !show
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}