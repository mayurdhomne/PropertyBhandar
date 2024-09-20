package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
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

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var signInButton: AppCompatButton
    private lateinit var networkClient: NetworkClient
    private var customLoadingDialog: CustomLoadingDialog? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpTextView = findViewById(R.id.signUpTextView)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        signInButton = findViewById(R.id.signInButton)

        networkClient = NetworkClient(this)

        signInButton.setOnClickListener { signIn() }

        signUpTextView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()  // Close the current LoginActivity
        }


    }


    private fun signIn() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // Validate Email
        if (!isValidEmail(email)) {
            emailEditText.error = "Please enter a valid email address."
            Toast.makeText(this, "Invalid email format. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordEditText.error = "Password cannot be empty."
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show the loading dialog
        showLoadingDialog()

        networkClient.loginUser(email, password, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Hide the loading dialog
                    hideLoadingDialog()
                    Toast.makeText(this@LoginActivity, "Login failed due to network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    // Hide the loading dialog
                    hideLoadingDialog()
                    if (response.isSuccessful) {
                        // Save login state
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().apply {
                            putBoolean("isLoggedIn", true)
                            apply()
                        }

                        // Show success message
                        Toast.makeText(this@LoginActivity, "Login Successful! Welcome to PropertyBhandar!", Toast.LENGTH_SHORT).show()

                        // Redirect to HomeActivity
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        handleLoginErrorResponse(response)
                    }
                }
            }
        })
    }

    private fun handleLoginErrorResponse(response: Response) {
        when (response.code) {
            400 -> {
                Toast.makeText(this@LoginActivity, "Login failed. Please verify your email and password, and try again.", Toast.LENGTH_LONG).show()
            }
            401 -> {
                Toast.makeText(this@LoginActivity, "Unauthorized access. Please check your credentials and ensure your account is verified.", Toast.LENGTH_LONG).show()
            }
            500 -> {
                Toast.makeText(this@LoginActivity, "We're experiencing technical difficulties. Please try again later.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this@LoginActivity, "An unexpected error occurred: ${response.message}. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()  // Close the current LoginActivity
    }


}
