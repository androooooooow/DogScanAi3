package com.firstapp.dogscanai.accounts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.ActivityDashboardBinding
import com.firstapp.dogscanai.fragment_activity.HomeFragment
// Siguraduhin na tama ang package path ng CameraActivity mo
import fragment_activity.CameraActivity
import fragment_activity.ProfileFragment
import fragment_activity.SearchFragment
import inbox.InboxFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DASHBOARD", "=== DASHBOARD ACTIVITY STARTED ===")

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNavigationView.selectedItemId = R.id.navigation_home
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> { loadFragment(HomeFragment()); true }
                R.id.navigation_search -> { loadFragment(SearchFragment()); true }
                R.id.navigation_inbox -> { loadFragment(InboxFragment()); true }
                R.id.navigation_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }

        // Hide placeholder item for the FAB gap
        binding.bottomNavigationView.menu.findItem(R.id.navigation_placeholder)?.apply {
            isEnabled = false
            isVisible = false
        }

        // FIXED: Click Listener for Camera FAB
        binding.fabCamera.setOnClickListener {
            Log.d("DASHBOARD", "Camera FAB clicked")
            try {
                // Tiyakin na ang CameraActivity ay declared sa Manifest
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("DASHBOARD", "Error starting CameraActivity: ${e.message}")
                Toast.makeText(this, "Camera screen not found or declared!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } catch (e: Exception) {
            Log.e("DASHBOARD", "Error loading fragment: ${e.message}")
        }
    }
}