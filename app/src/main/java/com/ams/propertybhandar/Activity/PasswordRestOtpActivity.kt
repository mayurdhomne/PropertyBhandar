package com.ams.propertybhandar.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class PasswordRestOtpActivity : AppCompatActivity() {

    private lateinit var otpEditTexts: List<EditText>
    private lateinit var verifyButton: AppCompatButton
    private lateinit var networkClient: NetworkClient
    private lateinit var email: String
    private var customLoadingDialog: CustomLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_rest_otp)

        otpEditTexts = listOf(
            findViewById(R.id.otpEditText1),
            findViewById(R.id.otpEditText2),
            findViewById(R.id.otpEditText3),
            findViewById(R.id.otpEditText4),
            findViewById(R.id.otpEditText5),
            findViewById(R.id.otpEditText6)
        )
        verifyButton = findViewById(R.id.signInButton)
        networkClient = NetworkClient(this)

        email = intent.getStringExtra("email") ?: ""

        // Set TextWatcher for each EditText
        for (i in otpEditTexts.indices) {
            otpEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < otpEditTexts.size - 1) {
                            otpEditTexts[i + 1].requestFocus()  // Move to the next EditText
                        } else {
                            // Last OTP field filled, automatically verify OTP
                            verifyOtp()
                        }
                    } else if (s?.length == 0 && i > 0) {
                        otpEditTexts[i - 1].requestFocus()  // Move to the previous EditText if backspace
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        verifyButton.setOnClickListener {
            verifyOtp()  // Manual verification when the button is clicked
        }
    }

    private fun verifyOtp() {
        val otp = otpEditTexts.joinToString(separator = "") { it.text.toString().trim() }
        if (otp.length == 6) {
            showLoadingDialog()
            networkClient.verifyPasswordResetOtp(email, otp, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        hideLoadingDialog()
                        Toast.makeText(this@PasswordRestOtpActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        hideLoadingDialog()
                        if (response.isSuccessful) {
                            try {
                                val responseBody = response.body?.string() ?: ""
                                val jsonObject = JSONObject(responseBody)
                                val message = jsonObject.getString("message")

                                Toast.makeText(
                                    this@PasswordRestOtpActivity,
                                    "OTP verified successfully. Please proceed to reset your password.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Proceed to PasswordResetActivity
                                val intent = Intent(this@PasswordRestOtpActivity, PasswordResetActivity::class.java)
                                intent.putExtra("otp", otp)
                                startActivity(intent)
                                finish()

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(this@PasswordRestOtpActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@PasswordRestOtpActivity, "Verification failed: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            Toast.makeText(this@PasswordRestOtpActivity, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
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
