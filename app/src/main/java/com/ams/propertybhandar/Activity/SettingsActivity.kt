package com.ams.propertybhandar.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Back Button Click Listener
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish() // Go back to the previous activity
        }

        // Account overview click listener
        findViewById<LinearLayout>(R.id.llAccountOverview).setOnClickListener {
            if (isLoggedIn()) {
                // User is logged in, navigate to AccountOverviewActivity
                val intent = Intent(this, AccountOverviewActivity::class.java)
                startActivity(intent)
            } else {
                // Show an advanced dialog to the user, prompting them to log in
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Login Required")
                builder.setMessage("You need to log in to view your account overview. Please log in to access this feature.")
                builder.setIcon(R.drawable.ic_warning) // Optional: Add an icon for better UX

                builder.setPositiveButton("Log in Now") { dialog, _ ->
                    // Navigate to LoginActivity
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    dialog.dismiss()
                    finish() // Optional: Close HomeActivity after navigating to LoginActivity
                }

                builder.setNegativeButton("Cancel") { dialog, _ ->
                    // Just dismiss the dialog
                    dialog.dismiss()
                }

                // Create and show the dialog
                val dialog = builder.create()

                // Set custom background (if required for consistency with your app design)
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

                dialog.show()
            }
        }


        // Privacy Centre click listener
        findViewById<LinearLayout>(R.id.llPrivacyCentre).setOnClickListener {
            val intent = Intent(this, CoustomerServiceActivity::class.java)
            startActivity(intent)
        }

        // Terms & conditions click listener
        findViewById<LinearLayout>(R.id.llTermsConditions).setOnClickListener {
            val url = "https://propertybhandar.com/privacy_policy/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // Check for updates click listener
        // Check for updates click listener
        findViewById<LinearLayout>(R.id.llcheckforupdates).setOnClickListener {
            val appPackageName = packageName // Get the current app package name
            try {
                // Try to open the Play Store app if it's available
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                // If the Play Store app isn't available, open the browser instead
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
                startActivity(intent)
            }
        }



    }

    // Method to open email client when email is clicked
    fun onEmailClick(view: View) {
        composeEmail("info@aartimultiservices.com")
    }
    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false) // Adjust based on how login status is stored
    }

    private fun composeEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
        }
        startActivity(intent)
    }
}
