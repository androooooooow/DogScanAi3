package com.firstapp.dogscanai.accounts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.ActivityDashboardBinding
// REMOVE THIS LINE: import fragment_activity.*  // ← ITO ANG PROBLEMA!

// Import fragments from the correct package
import fragment_activity.CameraActivity  // ← Maling package! // Make sure this is in com.firstapp.dogscanai
import fragment_activity.HomeFragment     // Should be in com.firstapp.dogscanai
import fragment_activity.ProfileFragment  // Should be in com.firstapp.dogscanai
import fragment_activity.SearchFragment   // Should be in com.firstapp.dogscanai
import inbox.InboxFragment  // Should be in com.firstapp.dogscanai.inbox


import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import network.model.AuthManager
import network.model.RetrofitClient

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AuthManager
        AuthManager.initialize(this)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Check if logged in
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // Setup Bottom Navigation based on YOUR menu
        setupBottomNavigation()

        // Load initial fragment (Home by default)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        bottomNav = binding.bottomNavigationView

        // Remove the default animation/behavior of the middle placeholder
        bottomNav.menu.findItem(R.id.navigation_placeholder).isEnabled = false

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_search -> {
                    // Load SearchFragment or start SearchActivity
                    loadFragment(SearchFragment())
                    true
                }
                R.id.navigation_inbox -> {
                    // Changed from Settings to Inbox
                    loadFragment(InboxFragment())
                    true
                }
                R.id.navigation_profile -> {
                    // Load ProfileFragment with user data
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            val user = sessionManager.getUser()
                            user?.let {
                                putString("user_id", it.id)
                                putString("user_name", it.name)
                                putString("user_email", it.email)
                            }
                        }
                    }
                    loadFragment(profileFragment)
                    true
                }
                else -> false
            }
        }

        // Add custom click listener for the middle (camera) area
        binding.fabCamera.setOnClickListener {
            // Open CameraActivity or CameraFragment
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    @SuppressLint("SetTextI18n")
    private fun displayUserInfo() {
        val user = sessionManager.getUser()
        if (user != null) {
            // You can display this in the ProfileFragment or in a header
            binding.toolbar.title = "Welcome, ${user.name}!"

            // For debugging
            android.util.Log.d("DASHBOARD", "User: ${user.name} | ${user.email}")
        }
    }

    private fun logoutUser() {
        val token = AuthManager.getBearerToken()

        lifecycleScope.launch {
            try {
                // Try server logout
                token?.let {
                    val response = RetrofitClient.getClient().logout(it)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@DashboardActivity,
                            "Logged out successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors for logout
                android.util.Log.e("DASHBOARD", "Logout error: ${e.message}")
            } finally {
                // Clear all local sessions
                sessionManager.clearSession()
                AuthManager.logout()

                // Go back to login
                goToLogin()
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Exit app when back pressed from Dashboard
        if (supportFragmentManager.backStackEntryCount == 0) {
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }
}