package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var networkClient: NetworkClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Initialize NetworkClient
        networkClient = NetworkClient(this)

        // Apply window insets to handle edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Apply fade-in animation to the logo
        val logo = findViewById<ImageView>(R.id.logoImageView)
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000 // Duration of the fade-in effect
        fadeIn.fillAfter = true
        logo.startAnimation(fadeIn)

        // Navigate to the next screen after the animation ends
        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // Delay to keep the splash screen visible for a while
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToHomeActivity() // Always navigate to HomeActivity
                }, 1000) // 1 second delay
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private fun navigateToHomeActivity() {
        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        finish()
    }
}

