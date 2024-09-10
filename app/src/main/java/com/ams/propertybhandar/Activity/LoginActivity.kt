package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
    }

    private fun signIn() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (!isValidEmail(email)) {
            emailEditText.error = "Invalid email"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password cannot be empty"
            return
        }

        // Show the loading dialog
        showLoadingDialog()

        networkClient.loginUser(email, password, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Hide the loading dialog
                    hideLoadingDialog()
                    Toast.makeText(this@LoginActivity, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
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

                        // Show the default success Toast
                        Toast.makeText(this@LoginActivity, "Login Successful! Welcome to PropertyBhandar!", Toast.LENGTH_SHORT).show()

                        // Redirect to HomeActivity
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Sign in failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
}
