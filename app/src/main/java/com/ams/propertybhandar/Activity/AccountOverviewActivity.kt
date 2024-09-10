package com.ams.propertybhandar.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AccountOverviewActivity : AppCompatActivity() {

    private lateinit var tvEmail: TextView
    private lateinit var btnDeleteAccount: Button
    private lateinit var networkClient: NetworkClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_overview)

        tvEmail = findViewById(R.id.tvEmail)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        networkClient = NetworkClient(this)
        val backButton: ImageView = findViewById(R.id.ivBack)


        backButton.setOnClickListener {
            onBackPressed()
        }

        fetchUserEmail()
        setupDeleteAccountButton()
    }

    private fun fetchUserEmail() {
        networkClient.fetchUserProfile(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    tvEmail.text = "Failed to fetch email"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let {
                        val responseData = it.string()
                        val jsonObject = JSONObject(responseData)
                        val userEmail = jsonObject.getJSONObject("user").getString("email")
                        runOnUiThread {
                            tvEmail.text = userEmail
                        }
                    }
                } else {
                    runOnUiThread {
                        tvEmail.text = "Failed to fetch email"
                    }
                }
            }
        })
    }

    private fun setupDeleteAccountButton() {
        btnDeleteAccount.setOnClickListener {
            // Define the URL you want to open
            val url = "https://www.propertybhandar.com/delete_account/"

            // Create an Intent to open the URL
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            // Start the activity to open the URL
            startActivity(intent)
        }
    }

}
