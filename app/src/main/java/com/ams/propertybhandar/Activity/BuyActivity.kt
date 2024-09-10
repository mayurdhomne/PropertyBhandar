package com.ams.propertybhandar.Activity


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R


class BuyActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        val feedbackButton: Button = findViewById(R.id.explore_btn)
        val backButton: ImageView = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            onBackPressed()
        }

        feedbackButton.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)

        }
    }
}
