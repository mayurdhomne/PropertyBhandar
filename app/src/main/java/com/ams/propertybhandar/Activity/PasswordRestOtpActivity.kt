package com.ams.propertybhandar.Activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
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
import java.util.concurrent.TimeUnit

class PasswordRestOtpActivity : AppCompatActivity() {

    private lateinit var otpEditTexts: List<EditText>
    private lateinit var verifyButton: AppCompatButton
    private lateinit var networkClient: NetworkClient
    private lateinit var email: String
    private lateinit var resendOtpButton: TextView // Add resend OTP button
    private lateinit var timerTextView: TextView // Add the timer TextView
    private var countDownTimer: CountDownTimer? = null // Timer variable
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

        resendOtpButton = findViewById(R.id.resendOtppassTextView) // Get the resend button
        resendOtpButton.setOnClickListener {
            resendOtp() // Call the resend function
        }

        timerTextView = findViewById(R.id.timerpassTextView) // Get the timer TextView
        timerTextView.text = "00:45" // Initial timer value

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
    private fun resendOtp() {
        val email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "")

        email?.let {
            showLoadingDialog()
            networkClient.resendpassOtp(it, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        hideLoadingDialog()
                        Toast.makeText(this@PasswordRestOtpActivity, "Resend OTP failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        hideLoadingDialog()
                        if (response.isSuccessful) {
                            Toast.makeText(this@PasswordRestOtpActivity, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                            startTimer()
                        } else {
                            Toast.makeText(this@PasswordRestOtpActivity, "Resend OTP failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } ?: run {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(45000, 1000) { // 45 seconds in milliseconds
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes)
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                resendOtpButton.isEnabled = true // Enable resend button after timer finishes
            }
        }.start()
        resendOtpButton.isEnabled = false // Disable resend button while timer is running
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
