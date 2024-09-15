package com.ams.propertybhandar.Activity

import android.app.AlertDialog
import android.content.DialogInterface
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
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AccountOverviewActivity : AppCompatActivity() {

    private lateinit var tvEmail: TextView
    private lateinit var btnDeleteAccount: Button
    private lateinit var networkClient: NetworkClient
    private var userId: Int = 0  // Assume user ID will be fetched from the profile

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
                        userId = jsonObject.getJSONObject("user").getInt("id")  // Save user ID
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
            // Show a confirmation dialog before deleting the account
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Delete Account")
            dialogBuilder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            dialogBuilder.setPositiveButton("Delete") { dialog, which ->
                // Proceed with account deletion
                deleteAccount()
            }
            dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
                // Dismiss the dialog
                dialog.dismiss()
            }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }

    private fun deleteAccount() {
        val deleteUrl = "https://propertybhandar.com/api/users/$userId/"
        val request = Request.Builder()
            .url(deleteUrl)
            .delete()
            .addHeader("Authorization", "Bearer ${networkClient.getAccessToken()}") // Add access token
            .build()

        networkClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Show an error message if the network request fails
                    showErrorDialog("Account deletion failed. Please try again.")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        // Account successfully deleted, handle UI updates here
                        showSuccessDialog("Your account has been successfully deleted.")
                    } else {
                        // Handle the failure case
                        showErrorDialog("Failed to delete the account. Please try again later.")
                    }
                }
            }
        })
    }


    private fun showErrorDialog(message: String) {
        val errorDialogBuilder = AlertDialog.Builder(this)
        errorDialogBuilder.setTitle("Error")
        errorDialogBuilder.setMessage(message)
        errorDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        val errorDialog = errorDialogBuilder.create()
        errorDialog.show()
    }

    private fun showSuccessDialog(message: String) {
        val successDialogBuilder = AlertDialog.Builder(this)
        successDialogBuilder.setTitle("Success")
        successDialogBuilder.setMessage(message)
        successDialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            finish() // Close the activity after account deletion
        })
        val successDialog = successDialogBuilder.create()
        successDialog.show()
    }
}
