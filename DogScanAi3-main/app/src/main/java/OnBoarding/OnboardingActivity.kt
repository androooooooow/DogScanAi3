package com.firstapp.dogscanai.OnBoarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.accounts.LoginActivity
import com.firstapp.dogscanai.accounts.DashboardActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var getStartedButton: Button

    // Launcher para sa Camera Permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied. You can enable it in settings.", Toast.LENGTH_SHORT).show()
            }
            // Kahit granted o denied, itutuloy natin sa Login para hindi ma-stuck ang user
            navigateToLogin()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Check Session - Kung tapos na mag-onboarding dati, redirect agad
        sessionManager = SessionManager(this)
        if (!sessionManager.isFirstTimeLaunch()) {
            redirectUser()
            return
        }

        setContentView(R.layout.activity_onboarding)

        // 2. Initialize Views
        viewPager = findViewById(R.id.onboarding_view_pager)
        indicatorContainer = findViewById(R.id.indicator_container)
        getStartedButton = findViewById(R.id.get_started_button)

        // 3. Setup ViewPager Adapter
        val layouts = listOf(
            R.layout.onboarding_slide_1,
            R.layout.onboarding_slide_2,
            R.layout.onboarding_slide_3
        )
        onboardingAdapter = OnboardingAdapter(layouts)
        viewPager.adapter = onboardingAdapter

        // 4. Setup Indicators (dots sa ilalim)
        setupIndicators()
        setCurrentIndicator(0)

        // 5. Page Change Callback
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        // 6. BUTTON CLICK LOGIC
        // Dito natin ilalagay ang pagpunta sa Login
        getStartedButton.setOnClickListener {
            checkPermissionAndNavigate()
        }
    }

    private fun checkPermissionAndNavigate() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // Kung wala pang permission, hihingi tayo
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Kung meron na, diretso na sa Login
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        // Markahan na tapos na ang onboarding para hindi na bumalik dito next time
        sessionManager.setFirstTimeLaunch(false)

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Isasara ang onboarding activity
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

    private fun setupIndicators() {
        indicatorContainer.removeAllViews()
        val indicators = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(25, 25).apply {
            setMargins(10, 0, 10, 0)
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
            if (i == index) {
                img.setImageResource(R.drawable.indicator_active)
            } else {
                img.setImageResource(R.drawable.indicator_inactive)
            }
        }
    }
}