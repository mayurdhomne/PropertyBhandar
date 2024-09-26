package com.ams.propertybhandar.Activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject

class CoustomerServiceActivity : AppCompatActivity() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var mobileInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var subjectInputLayout: TextInputLayout
    private lateinit var descriptionInputLayout: TextInputLayout
    private lateinit var submitButton: MaterialButton
    private lateinit var backButton: ImageView

    private val apiUrl = "https://www.propertybhandar.com/api/contact/"
    private var customLoadingDialog: CustomLoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coustomer_service)

        // Initialize views
        nameInputLayout = findViewById(R.id.name_input_layout)
        mobileInputLayout = findViewById(R.id.mobile_input_layout)
        emailInputLayout = findViewById(R.id.email_input_layout)
        subjectInputLayout = findViewById(R.id.subject)
        descriptionInputLayout = findViewById(R.id.description)
        submitButton = findViewById(R.id.submit_button)
        backButton = findViewById(R.id.ivBack)
        backButton.setOnClickListener {
            onBackPressed()
        }

        submitButton.setOnClickListener { submitForm() }
    }

    private fun submitForm() {
        val name = nameInputLayout.editText?.text.toString().trim()
        val contactNumber = mobileInputLayout.editText?.text.toString().trim()
        val email = emailInputLayout.editText?.text.toString().trim()
        val subject = subjectInputLayout.editText?.text.toString().trim()
        val message = descriptionInputLayout.editText?.text.toString().trim()

        // Validate inputs
        if (name.isEmpty() || contactNumber.isEmpty() || email.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show the loading dialog
        showLoadingDialog()

        // Create JSON request body
        val jsonBody = JSONObject().apply {
            put("name", name)
            put("contact_number", contactNumber)
            put("email", email)
            put("subject", subject)
            put("message", message)
        }

        // Create request
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, apiUrl, jsonBody,
            { response ->
                // Handle successful response
                hideLoadingDialog()
                val messageResponse = response.optString("message", "Submission successful")
                Toast.makeText(this, messageResponse, Toast.LENGTH_SHORT).show()
                clearFields()
            },
            { error: VolleyError ->
                // Handle error response
                hideLoadingDialog()
                Toast.makeText(this, "Failed to submit form", Toast.LENGTH_SHORT).show()
                error.printStackTrace()
            }
        )

        // Add request to queue
        requestQueue.add(jsonObjectRequest)
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

    private fun clearFields() {
        nameInputLayout.editText?.text?.clear()
        mobileInputLayout.editText?.text?.clear()
        emailInputLayout.editText?.text?.clear()
        subjectInputLayout.editText?.text?.clear()
        descriptionInputLayout.editText?.text?.clear()
    }
}
