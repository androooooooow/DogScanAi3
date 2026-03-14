package fragment_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import inbox.InboxFragment

class StartActivity : AppCompatActivity() {

    companion object {
        var navigateToScanResult = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        val fabCamera: FloatingActionButton = findViewById(R.id.fab_camera)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.menu.findItem(R.id.navigation_placeholder).isEnabled = false

        fabCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment? = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_contribution -> ContributorLeaderboardFragment()
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
        if (navigateToScanResult) {
            navigateToScanResult = false
            loadFragment(DogScanResultFragment())
            findViewById<BottomNavigationView>(R.id.bottom_navigation_view).selectedItemId = R.id.navigation_placeholder
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}