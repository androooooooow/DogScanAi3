package com.firstapp.dogscanai.OnBoarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.dogscanai.utils.SessionManager // Import your SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.accounts.LoginActivity
import com.firstapp.dogscanai.accounts.SignupActivity
import com.firstapp.dogscanai.accounts.DashboardActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var sessionManager: SessionManager // Declare SessionManager

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val msg = if (isGranted) "Camera permission granted!" else "Camera permission denied"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            navigateToSignup()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize SessionManager
        sessionManager = SessionManager(this)

        // 2. CHECK ROUTING: Is it really the first time?
        if (!sessionManager.isFirstTimeLaunch()) {
            redirectUser()
            return
        }

        setContentView(R.layout.activity_onboarding)

        val layouts = listOf(
            R.layout.onboarding_slide_1,
            R.layout.onboarding_slide_2,
            R.layout.onboarding_slide_3
        )

        onboardingAdapter = OnboardingAdapter(layouts)
        viewPager = findViewById(R.id.onboarding_view_pager)
        viewPager.adapter = onboardingAdapter

        indicatorContainer = findViewById(R.id.indicator_container)
        setupIndicators()
        setCurrentIndicator(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                // Only trigger camera/navigation logic if it's the absolute last page
                if (position == onboardingAdapter.itemCount - 1) {
                    handleCameraSlide(position)
                }
            }
        })
    }

    private fun redirectUser() {
        val destination = if (sessionManager.isLoggedIn()) {
            DashboardActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }

    private fun handleCameraSlide(position: Int) {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            navigateToSignup()
        }
    }

    private fun navigateToSignup() {
        // 3. Mark onboarding as SEEN
        sessionManager.setFirstTimeLaunch(false)

        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    // setupIndicators and setCurrentIndicator remain the same...
    private fun setupIndicators() {
        indicatorContainer.removeAllViews() // Clear existing to prevent duplicates
        val indicators = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(12, 12).apply {
            setMargins(8, 0, 8, 0)
        }
        for (i in indicators.indices) {
            indicators[i] = ImageView(this).apply {
                setImageResource(R.drawable.indicator_inactive)
                this.layoutParams = layoutParams
            }
            indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        for (i in 0 until indicatorContainer.childCount) {
            val img = indicatorContainer.getChildAt(i) as ImageView
            img.setImageResource(
                if (i == index) R.drawable.indicator_active else R.drawable.indicator_inactive
            )
        }
    }
}