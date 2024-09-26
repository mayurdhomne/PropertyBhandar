package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

@Suppress("SameParameterValue")
class ContactUsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ContactUsActivity"
        private const val URL = "https://www.propertybhandar.com/api/contact/"
    }

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextSubject: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var webView: WebView
    private var customLoadingDialog: CustomLoadingDialog? = null
    private lateinit var networkClient: NetworkClient
    private var propertyId: String? = null

    @SuppressLint("MissingInflatedId", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)
        networkClient = NetworkClient(this)

        // Get property ID from the intent
        propertyId = intent.getStringExtra("property_id")

        // Check if user is logged in
        if (networkClient.getAccessToken() == null) {
            showLoginRequiredDialog()
        }

        // Initialize views
        editTextName = findViewById(R.id.name)
        editTextEmail = findViewById(R.id.email)
        editTextPhone = findViewById(R.id.phone)
        editTextSubject = findViewById(R.id.subject)
        editTextMessage = findViewById(R.id.message)
        webView = findViewById(R.id.webview)

        // Setup WebView
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        if (intent != null) {
                            startActivity(intent)
                            return true
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Intent URI failed: ${e.message}")
                    }
                }
                return false
            }
        }

        // Load Google Maps embed HTML
        val html = "<html><body style=\"margin:0;padding:0;\">" +
                " <iframe src=\"https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3721.4530335494323!2d79.0978809750839!3d21.13436188054154!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3bd4c113c0ba3c41%3A0xc80feea5406c31a5!2sAarti%20Multi%20Services%20Private%20Limited!5e0!3m2!1sen!2sin!4v1725263366987!5m2!1sen!2sin\" width=\"600\" height=\"450\" style=\"border:0;\" allowfullscreen=\"\" loading=\"lazy\" referrerpolicy=\"no-referrer-when-downgrade\"></iframe>" +
                "</body></html>"
        webView.loadData(html, "text/html", "UTF-8")

        // Setup send message button click listener
        val sendMessageButton: Button = findViewById(R.id.sendMessageButton)
        sendMessageButton.setOnClickListener { sendMessage() }

        // Make back arrow clickable
        val backArrow: ImageView = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        customLoadingDialog?.dismiss()
        super.onDestroy()
    }

    private fun sendMessage() {
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val phone = editTextPhone.text.toString().trim()
        val subject = editTextSubject.text.toString().trim()
        val message = editTextMessage.text.toString().trim()

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
            TextUtils.isEmpty(subject) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading dialog before sending the message
        showLoadingDialog()

        // Use the `propertyId` if available to make the subject more specific
        val finalSubject = if (propertyId != null) {
            "/nUser inquiry for property ID: $propertyId - $subject"
        } else {
            subject
        }

        JSONObject().apply {
            put("name", name)
            put("email", email)
            put("contact_number", phone)
            put("subject", finalSubject)
            put("message", message)
        }

        networkClient.submitContactForm(name, email, message, finalSubject, phone, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Switch to main thread to show the error Toast
                runOnUiThread {
                    Toast.makeText(this@ContactUsActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    hideLoadingDialog()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Ensure response is successful before attempting to parse
                if (!response.isSuccessful) {
                    runOnUiThread {
                        val errorMessage = "Error: ${response.code} - ${response.message}"
                        Toast.makeText(this@ContactUsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        hideLoadingDialog()
                    }
                    return
                }

                // Try to parse the response as JSON
                try {
                    val jsonResponse = response.body?.string() ?: throw JSONException("Empty Response")

                    // Check if the response contains valid JSON data
                    if (jsonResponse.startsWith("{")) {
                        val jsonObject = JSONObject(jsonResponse)
                        val responseMessage = jsonObject.getString("message")

                        // Update UI on the main thread
                        runOnUiThread {
                            Toast.makeText(this@ContactUsActivity, responseMessage, Toast.LENGTH_SHORT).show()
                            clearFields() // Clear input fields after submission
                            hideLoadingDialog()
                        }
                    } else {
                        // Handle cases where the response is not JSON
                        runOnUiThread {
                            Log.e(TAG, "Unexpected response format: $jsonResponse")
                            Toast.makeText(this@ContactUsActivity, "Unexpected server response", Toast.LENGTH_SHORT).show()
                            hideLoadingDialog()
                        }
                    }
                } catch (e: JSONException) {
                    // Handle JSON parsing errors
                    runOnUiThread {
                        Log.e(TAG, "JSON Parsing error: ${e.message}")
                        Toast.makeText(this@ContactUsActivity, "Parsing error", Toast.LENGTH_SHORT).show()
                        hideLoadingDialog()
                    }
                }
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun clearFields() {
        editTextName.setText("")
        editTextEmail.setText("")
        editTextPhone.setText("")
        editTextSubject.setText("")
        editTextMessage.setText("")
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

    // Method to initiate phone call when first phone number is clicked
    fun onPhone1Click(view: View) {
        dialPhoneNumber("+91 9067000315")
    }

    // Method to initiate phone call when second phone number is clicked
    fun onPhone2Click(view: View) {
        dialPhoneNumber("+91 9067000316")
    }

    // Method to open email client when email is clicked
    fun onEmailClick(view: View) {
        composeEmail("info@aartimultiservices.com")
    }

    // Method to dial a phone number
    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    // Method to compose an email
    private fun composeEmail(emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
        }
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun showLoginRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_required, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        // Customize the dialog UI elements
        val titleTextView: TextView = dialogView.findViewById(R.id.dialogTitle)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialogMessage)
        val loginButton: CardView = dialogView.findViewById(R.id.btnLoginNow)
        val cancelButton: CardView = dialogView.findViewById(R.id.btnMaybeLater)

        titleTextView.text = "Login Required"
        messageTextView.text = "You need to be logged in to add a property."

        loginButton.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Customize the dialog appearance (optional)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        // Make the dialog non-cancelable (no cancel button or back press)

        dialog.setCanceledOnTouchOutside(false)

        dialog.show()
    }
}