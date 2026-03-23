package com.firstapp.dogscanai.accounts  // ✅ palitan ito

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.firstapp.dogscanai.databinding.ActivityForgotPasswordBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import network.api.RetrofitClient
import kotlin.collections.get

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.loginLink.setOnClickListener { finish() }

        binding.btnSendReset.setOnClickListener { attemptReset() }
    }

    private fun attemptReset() {
        val email = binding.email.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getClient()
                    .forgotPassword(mapOf("email" to email))

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Reset link sent! Check your email.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        Gson().fromJson(errorBody, Map::class.java)["error"] as? String
                            ?: "Something went wrong"
                    } catch (e: Exception) { "Something went wrong" }
                    Toast.makeText(this@ForgotPasswordActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Network Error", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSendReset.isEnabled = !show
    }
}