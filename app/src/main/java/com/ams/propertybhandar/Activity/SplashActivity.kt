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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

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
                    checkTokenAndNavigate()
                }, 1000) // 1 second delay
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private fun checkTokenAndNavigate() {
        val accessToken = networkClient.getAccessToken()
        if (accessToken != null) {
            // Token exists, check if it is expired
            networkClient.makeAuthenticatedRequest(
                url = "https://propertybhandar.com/api/profiles/profile/",
                request = Request.Builder().url("https://propertybhandar.com/api/profiles/profile/").build(),
                callback = object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // Token might be expired or request failed, attempt to refresh token
                        handleTokenRefresh()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            // Token is valid, navigate to HomeActivity
                            navigateToHomeActivity()
                        } else if (response.code == 401) {
                            // Token expired, attempt to refresh token
                            handleTokenRefresh()
                        } else {
                            // Handle other response codes if needed
                            navigateToLoginActivity()
                        }
                    }
                }
            )
        } else {
            // No access token, navigate to LoginActivity
            navigateToLoginActivity()
        }
    }

    private fun handleTokenRefresh() {
        val refreshToken = networkClient.getRefreshToken()
        if (refreshToken != null) {
            networkClient.refreshToken(refreshToken, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    navigateToLoginActivity() // Refresh failed, navigate to login
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        // Token refreshed successfully, retry accessing profile
                        checkTokenAndNavigate()
                    } else {
                        navigateToLoginActivity() // Refresh failed, navigate to login
                    }
                }
            })
        } else {
            // No refresh token, navigate to LoginActivity
            navigateToLoginActivity()
        }
    }

    private fun navigateToHomeActivity() {
        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        finish()
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
        finish()
    }
}
