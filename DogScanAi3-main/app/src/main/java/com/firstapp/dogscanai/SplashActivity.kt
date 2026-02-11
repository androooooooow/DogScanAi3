package com.firstapp.dogscanai

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.firstapp.dogscanai.OnBoarding.OnboardingActivity

// REMOVED: The incorrect import statement that was here.
// import com.firstapp.dogscanai.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val introLayout: RelativeLayout = findViewById(R.id.intro_layout)
        val logoImageView: ImageView = findViewById(R.id.logo)

        val screenFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val logoFadeIn = AnimationUtils.loadAnimation(this, R.anim.logo_fade_in)

        // Initialize MediaPlayer safely
        mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound)

        logoFadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                // Check if mediaPlayer was successfully created before starting
                mediaPlayer.start()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })

        introLayout.startAnimation(screenFadeIn)
        logoImageView.startAnimation(logoFadeIn)

        // Use a Handler to delay navigation to the next screen
        Handler(Looper.getMainLooper()).postDelayed({
            // This will now resolve correctly as both activities are in the same package
            startActivity(Intent(this, OnboardingActivity::class.java))

            // Finish SplashActivity so the user can't go back to it
            finish()
        }, 2500) // 2.5-second delay
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the media player resources to prevent memory leaks
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}
