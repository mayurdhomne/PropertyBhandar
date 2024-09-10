package com.ams.propertybhandar.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.CheckBox
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

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var signInTextView: TextView
    private lateinit var signUpButton: AppCompatButton
    private lateinit var networkClient: NetworkClient
    private var customLoadingDialog: CustomLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        contactEditText = findViewById(R.id.contactEditText)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        signInTextView = findViewById(R.id.signInText)
        signUpButton = findViewById(R.id.signUpButton)

        networkClient = NetworkClient(this)

        signUpButton.setOnClickListener {
            if (validateInputs()) {
                signUpButton.isEnabled = false // Disable button to prevent multiple clicks
                signUp()
            } else {
                signUpButton.isEnabled = true // Re-enable button if validation fails
            }
        }

        signInTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val contact = contactEditText.text.toString().trim()

        return when {
            !isValidEmail(email) -> {
                emailEditText.error = "Invalid email"
                false
            }
            contact.isEmpty() -> {
                contactEditText.error = "Contact cannot be empty"
                false
            }
            !isValidPassword(password) -> {
                passwordEditText.error = "Password must be at least 6 characters"
                false
            }
            !termsCheckBox.isChecked -> {
                Toast.makeText(this, "Please agree to the terms & conditions", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun signUp() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val contact = contactEditText.text.toString().trim()

        // Show the loading dialog
        showLoadingDialog()

        networkClient.registerUser(email, password, contact, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Hide the loading dialog
                    hideLoadingDialog()
                    Toast.makeText(this@SignUpActivity, "Sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    signUpButton.isEnabled = true // Re-enable button after failure
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    // Hide the loading dialog
                    hideLoadingDialog()
                    if (response.isSuccessful) {
                        saveUserData(email, contact, password)
                        showVerificationPopup()
                        Handler().postDelayed({
                            val intent = Intent(this@SignUpActivity, OtpVerificationActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, 500)
                    } else {
                        handleUnsuccessfulResponse(response)
                    }
                    signUpButton.isEnabled = true // Re-enable button after response
                }
            }
        })
    }

    private fun handleUnsuccessfulResponse(response: Response) {
        when (response.code) {
            400 -> {
                Toast.makeText(this@SignUpActivity, "Email or contact number already exists", Toast.LENGTH_SHORT).show()
            }
            500 -> Toast.makeText(this@SignUpActivity, "Server error, please try again later.", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this@SignUpActivity, "Sign up failed: ${response.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData(email: String, contact: String, password: String) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().apply {
            putString("email", email)
            putString("contact", contact)
            putString("password", password)
            apply()
        }
    }

    private fun showVerificationPopup() {
        // Show success message using default Toast
        Toast.makeText(this@SignUpActivity, "Account created successfully! Please check your email to verify your account.", Toast.LENGTH_LONG).show()
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

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}
