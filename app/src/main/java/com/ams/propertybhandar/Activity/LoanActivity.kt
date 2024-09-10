package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R

@Suppress("NAME_SHADOWING")
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
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, LoanApplicationActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

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