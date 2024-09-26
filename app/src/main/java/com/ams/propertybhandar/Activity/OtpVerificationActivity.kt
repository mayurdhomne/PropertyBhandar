package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
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
import java.io.IOException
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var networkClient: NetworkClient
    private var customLoadingDialog: CustomLoadingDialog? = null
    private lateinit var otpEditTexts: Array<EditText>
    private lateinit var verifyButton: AppCompatButton
    private lateinit var resendOtpButton: TextView // Add resend OTP button
    private lateinit var timerTextView: TextView // Add the timer TextView
    private var countDownTimer: CountDownTimer? = null // Timer variable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        networkClient = NetworkClient(this)

        otpEditTexts = arrayOf(
            findViewById(R.id.otpEditText1),
            findViewById(R.id.otpEditText2),
            findViewById(R.id.otpEditText3),
            findViewById(R.id.otpEditText4),
            findViewById(R.id.otpEditText5),
            findViewById(R.id.otpEditText6)
        )

        verifyButton = findViewById(R.id.signInButton)
        verifyButton.setOnClickListener {
            verifyOtp()
        }

        resendOtpButton = findViewById(R.id.resendOtpTextView) // Get the resend button
        resendOtpButton.setOnClickListener {
            resendOtp() // Call the resend function
        }

        timerTextView = findViewById(R.id.timerTextView) // Get the timer TextView
        timerTextView.text = "00:45" // Initial timer value

        // Set up TextWatchers for all OTP EditTexts
        for (i in otpEditTexts.indices) {
            otpEditTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        // Move to next field
                        if (i < otpEditTexts.size - 1) {
                            otpEditTexts[i + 1].requestFocus()
                        } else {
                            // Last OTP field filled, automatically verify OTP
                            verifyOtp()
                        }
                    } else if (s?.length == 0 && i > 0) {
                        // Move to previous field if backspace is pressed
                        otpEditTexts[i - 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        resendOtp()
    }

    private fun verifyOtp() {
        val otp = gatherOtp()
        val email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "")

        if (otp.isEmpty() || otp.length < 6) {
            Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            return
        }

        email?.let {
            showLoadingDialog()
            networkClient.verifyOtp(it, otp, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        hideLoadingDialog()
                        Toast.makeText(this@OtpVerificationActivity, "OTP verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        hideLoadingDialog()
                        if (response.isSuccessful) {
                            Toast.makeText(this@OtpVerificationActivity, "Verification successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@OtpVerificationActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@OtpVerificationActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } ?: run {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resendOtp() {
        val email = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("email", "")

        email?.let {
            showLoadingDialog()
            networkClient.resendOtp(it, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        hideLoadingDialog()
                        Toast.makeText(this@OtpVerificationActivity, "Resend OTP failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        hideLoadingDialog()
                        if (response.isSuccessful) {
                            Toast.makeText(this@OtpVerificationActivity, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                            startTimer()
                        } else {
                            Toast.makeText(this@OtpVerificationActivity, "Resend OTP failed", Toast.LENGTH_SHORT).show()
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

    private fun gatherOtp(): String {
        return otpEditTexts.joinToString("") { it.text.toString() }
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