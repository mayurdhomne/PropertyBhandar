package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
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
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ForgotActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: AppCompatButton
    private lateinit var networkClient: NetworkClient
    private var customLoadingDialog: CustomLoadingDialog? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)

        emailEditText = findViewById(R.id.emailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        networkClient = NetworkClient(this)

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this@ForgotActivity, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                showLoadingDialog()
                networkClient.forgotPassword(email, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            hideLoadingDialog()
                            Toast.makeText(this@ForgotActivity, "Forgot password request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            hideLoadingDialog()
                            if (response.isSuccessful) {
                                try {
                                    val jsonObject = JSONObject(response.body?.string() ?: "")
                                    val message = jsonObject.getString("message")
                                    Toast.makeText(this@ForgotActivity, message, Toast.LENGTH_SHORT).show()

                                    // Open PasswordRestOtpActivity
                                    val intent = Intent(this@ForgotActivity, PasswordRestOtpActivity::class.java)
                                    intent.putExtra("email", email) // Pass email to the next activity
                                    startActivity(intent)
                                    finish() // Optional: finish ForgotActivity if not needed anymore

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    Toast.makeText(this@ForgotActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@ForgotActivity, "Forgot password request failed: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
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
