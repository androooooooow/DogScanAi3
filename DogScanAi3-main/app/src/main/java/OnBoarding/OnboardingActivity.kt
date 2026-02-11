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
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.accounts.SignupActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var onboardingAdapter: OnboardingAdapter

    private var permissionJustGranted = false
    private var isLastPageSelected = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionJustGranted = isGranted
            val msg = if (isGranted) "Camera permission granted!" else "Camera permission denied"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            // proceed anyway
            navigateToSignup()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val layouts = listOf(
            R.layout.onboarding_slide_1,
            R.layout.onboarding_slide_2,
            R.layout.onboarding_slide_3 // Camera slide
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
                isLastPageSelected = position == onboardingAdapter.itemCount - 1
                handleCameraSlide(position)
            }
        })
    }

    private fun handleCameraSlide(position: Int) {
        val isCameraSlide = onboardingAdapter.isCameraSlide(position)
        if (isCameraSlide) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                // Ask for permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                navigateToSignup()
            }
        } else if (isLastPageSelected) {
            // If it's just the last slide (not camera), proceed
            navigateToSignup()
        }
    }

    private fun navigateToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onboardingAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(this).apply {
                setImageResource(R.drawable.indicator_inactive)
                this.layoutParams = layoutParams
            }
            indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val count = indicatorContainer.childCount
        for (i in 0 until count) {
            val img = indicatorContainer.getChildAt(i) as ImageView
            img.setImageResource(
                if (i == index) R.drawable.indicator_active else R.drawable.indicator_inactive
            )
        }
    }
}
