package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogscanai.models.ErrorResponse
import com.firstapp.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.databinding.ActivityLoginBinding
import com.firstapp.dogscanai.utils.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.model.AuthManager
import network.model.LoginRequest
import network.api.RetrofitClient

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.initialize(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        googleSignInClient = GoogleAuthHelper.getGoogleSignInClient(this)

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

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.googleSignInButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, GoogleAuthHelper.RC_SIGN_IN)
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
                Log.d("GOOGLE_AUTH", "email: ${account.email}")
                Log.d("GOOGLE_AUTH", "idToken: ${account.idToken}")

                val idToken = account.idToken
                if (idToken != null) {
                    sendGoogleTokenToBackend(idToken)
                } else {
                    Toast.makeText(this, "Google Sign-In failed: token is null", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Log.e("GOOGLE_AUTH", "ApiException: ${e.statusCode}")
                Toast.makeText(this, "Google Sign-In error: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendGoogleTokenToBackend(idToken: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getClient().googleAuth(
                    mapOf("idToken" to idToken)
                )

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // ✅ Block admin accounts on mobile
                    val isAdmin = authResponse.user?.is_admin == true ||
                            authResponse.user?.is_superadmin == true
                    if (isAdmin) {
                        showLoading(false)
                        googleSignInClient.signOut()
                        Toast.makeText(
                            this@LoginActivity,
                            "⚠️ Admin accounts are not allowed on mobile. Please use the web app.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

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
                    Log.e("GOOGLE_AUTH", "Backend error: $errorBody")
                    val errorMessage = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).error
                    } catch (e: Exception) { "Google Sign-In failed." }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "Network error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
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
                val response = RetrofitClient.getClient().login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    // ✅ Block admin accounts on mobile
                    val isAdmin = authResponse.user?.is_admin == true ||
                            authResponse.user?.is_superadmin == true
                    if (isAdmin) {
                        showLoading(false)
                        Toast.makeText(
                            this@LoginActivity,
                            "⚠️ Admin accounts are not allowed on mobile. Please use the web app.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

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
        binding.loginButton.isEnabled = !show
        binding.googleSignInButton.isEnabled = !show
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}