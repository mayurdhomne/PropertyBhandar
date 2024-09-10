package com.ams.propertybhandar.Activity

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var resetButton: AppCompatButton
    private lateinit var networkClient: NetworkClient
    private lateinit var otp: String
    private var customLoadingDialog: CustomLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        resetButton = findViewById(R.id.resetButton)
        networkClient = NetworkClient(this)

        otp = intent.getStringExtra("otp") ?: ""

        resetButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            when {
                newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                newPassword != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    showLoadingDialog()
                    networkClient.resetPassword(newPassword, confirmPassword, otp, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                hideLoadingDialog()
                                Toast.makeText(this@PasswordResetActivity, "Reset failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            runOnUiThread {
                                hideLoadingDialog()
                                if (response.isSuccessful) {
                                    try {
                                        val jsonObject = JSONObject(response.body?.string() ?: "")
                                        val message = jsonObject.getString("message")
                                        Toast.makeText(this@PasswordResetActivity, message, Toast.LENGTH_SHORT).show()

                                        // Optionally, redirect to login or other activities
                                        finish()

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(this@PasswordResetActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@PasswordResetActivity, "Reset failed: ${response.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    private fun showLoadingDialog() {
        if (customLoadingDialog == null) {
            customLoadingDialog = CustomLoadingDialog(this)
        }
        customLoadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        customLoadingDialog?.dismiss()
    }
}
