package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R

class LoanActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        val feedbackButton: Button = findViewById(R.id.explore_btn)
        val right_arrow: ImageView = findViewById(R.id.right_arrow)
        val right_arrow2: ImageView = findViewById(R.id.right_arrow2)
        val right_arrow3: ImageView = findViewById(R.id.right_arrow3)

        feedbackButton.setOnClickListener {
            // Check if the user is logged in
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                // User is logged in, navigate to LoanApplicationActivity
                val intent = Intent(this@LoanActivity, LoanApplicationActivity::class.java)
                startActivity(intent)
            } else {
                // Show dialog to notify the user to log in first
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Attention Required")
                builder.setMessage("You need to be logged in to apply for a loan. Please log in to continue.")
                builder.setIcon(R.drawable.ic_warning) // Optional: Add a relevant icon if needed

                builder.setPositiveButton("Log in Now") { dialog, _ ->
                    // Navigate to LoginActivity
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    dialog.dismiss()
                }

                builder.setNegativeButton("Maybe Later") { dialog, _ ->
                    // Just dismiss the dialog
                    dialog.dismiss()
                }

                val dialog = builder.create()

                // Set the background of the dialog to the custom drawable with white background and rounded corners
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

                dialog.show()
            }
        }
        right_arrow.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, EMICalculatorActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

        }
        right_arrow2.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, LoanEligibilityActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

        }
        right_arrow3.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, HomeBudgetCalculatorActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

        }
        val backArrow: ImageView = findViewById(R.id.back_button)
        backArrow.setOnClickListener {
            // Open Home when back arrow is clicked
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()  // Optional: Finish the current activity if you don't want to return to it


        }
    }
}