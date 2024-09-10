package com.ams.propertybhandar.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
            val intent = Intent(this, AccountOverviewActivity::class.java)
            startActivity(intent)
        }

        // Privacy Centre click listener
        findViewById<LinearLayout>(R.id.llPrivacyCentre).setOnClickListener {
            val intent = Intent(this, CoustomerServiceActivity::class.java)
            startActivity(intent)
        }

        // Terms & conditions click listener
        findViewById<LinearLayout>(R.id.llTermsConditions).setOnClickListener {
            val intent = Intent(this, TermsActivity::class.java)
            startActivity(intent)
        }
    }

    // Method to open email client when email is clicked
    fun onEmailClick(view: View) {
        composeEmail("info@aartimultiservices.com")
    }

    private fun composeEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
        }
        startActivity(intent)
    }
}
