package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

@Suppress("DEPRECATION")
class AccountOverviewActivity : AppCompatActivity() {

    private lateinit var tvEmail: TextView
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnResetPassword: Button
    private lateinit var btnSignOut: Button // Add sign-out button
    private lateinit var networkClient: NetworkClient
    private var userId: Int = 0  // Assume user ID will be fetched from the profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_overview)

        tvEmail = findViewById(R.id.tvEmail)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnResetPassword = findViewById(R.id.Passwordresetbutton)
        btnSignOut = findViewById(R.id.SignOutButton) // Initialize sign-out button
        networkClient = NetworkClient(this)
        val backButton: ImageView = findViewById(R.id.ivBack)

        backButton.setOnClickListener {
            onBackPressed()
        }

        fetchUserEmail()
        setupDeleteAccountButton()
        setupResetPasswordButton()
        setupSignOutButton() // Set up sign-out button
    }

    private fun setupResetPasswordButton() {
        btnResetPassword.setOnClickListener {
            // Show a confirmation dialog before resetting the password
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Reset Password")
            dialogBuilder.setMessage("Are you sure you want to reset your password?")
            dialogBuilder.setPositiveButton("Reset") { dialog, which ->
                // Reset the password
                resetPassword()
            }
            dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
                // Cancel the dialog
                dialog.dismiss()
            }
            val dialog = dialogBuilder.create()

            // Set custom background if needed
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

            dialog.show()
        }
    }

    private fun resetPassword() {
        val intent = Intent(this, ForgotActivity::class.java)
        startActivity(intent)
    }

    private fun setupSignOutButton() {
        btnSignOut.setOnClickListener {
            showLogoutDialog() // Show the logout confirmation dialog
        }
    }

    private fun showLogoutDialog() {
        // Show a bottom sheet dialog for logout confirmation
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_logout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val yesButton: CardView = bottomSheetView.findViewById(R.id.yesButton)
        val noButton: CardView = bottomSheetView.findViewById(R.id.noButton)

        yesButton.setOnClickListener {
            // Clear shared preferences (log out the user)
            val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity

            bottomSheetDialog.dismiss()
        }

        noButton.setOnClickListener {
            bottomSheetDialog.dismiss() // Dismiss the dialog if 'No' is clicked
        }

        bottomSheetDialog.show()
    }

    private fun fetchUserEmail() {
        networkClient.fetchUserProfile(object : Callback {
            @SuppressLint("SetTextI18n")
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    tvEmail.text = "Failed to fetch email"
                }
            }

            @SuppressLint("SetTextI18n")
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
                        // Account successfully deleted, navigate to LoginActivity
                        showSuccessDialog("Your account has been successfully deleted.") {
                            // Navigate to LoginActivity after the success dialog is dismissed
                            startActivity(Intent(this@AccountOverviewActivity, LoginActivity::class.java))
                            finish() // Finish the current activity
                        }
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
        errorDialogBuilder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        val errorDialog = errorDialogBuilder.create()
        errorDialog.show()
        errorDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun showSuccessDialog(message: String, onDismiss: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Success")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onDismiss() // Execute the lambda when the dialog is dismissed
            }
        val dialog = builder.create()
        builder.create().show()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }
}
