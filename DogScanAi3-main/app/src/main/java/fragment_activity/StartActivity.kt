package fragment_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import fragment_activity.CameraActivity
import fragment_activity.DogScanResultFragment
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.fragment_activity.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import inbox.InboxFragment

class StartActivity : AppCompatActivity() {

    // Companion object to hold the navigation flag
    companion object {
        var navigateToScanResult = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        val fabCamera: FloatingActionButton = findViewById(R.id.fab_camera)

        // Load the default fragment when the app starts
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Disable the placeholder in the menu
        bottomNav.menu.findItem(R.id.navigation_placeholder).isEnabled = false

        // Set click listener for the camera button
        fabCamera.setOnClickListener {
            // Open the actual CameraActivity
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // Set click listener for the bottom navigation items
        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment? = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_result -> DogScanResultFragment()
                R.id.navigation_profile -> ProfileFragment()
                else -> null
            }

            selectedFragment?.let {
                loadFragment(it)
                return@setOnItemSelectedListener true
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if we need to navigate to the scan result screen
        if (navigateToScanResult) {
            navigateToScanResult = false // Reset the flag
            loadFragment(DogScanResultFragment())
            // De-select the bottom nav item
            findViewById<BottomNavigationView>(R.id.bottom_navigation_view).selectedItemId = R.id.navigation_placeholder
        }
    }

    // Helper function to swap fragments
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}