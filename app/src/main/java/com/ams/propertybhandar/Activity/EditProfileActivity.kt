package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

@Suppress("DEPRECATION")
class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val PREF_FIRST_NAME = "firstName"
        private const val PREF_LAST_NAME = "lastName"
        private const val PREF_EMAIL = "email"
        private const val PREF_CONTACT = "contact"
        private const val PREF_USER_ID = "userId"
    }

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backIcon: ImageView
    private lateinit var networkClient: NetworkClient

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firstNameEditText = findViewById(R.id.firstnameEditText)
        lastNameEditText = findViewById(R.id.lastnameEditText)
        emailEditText = findViewById(R.id.etEditEmail)
        contactEditText = findViewById(R.id.etEditContact)
        saveButton = findViewById(R.id.saveButton)
        backIcon = findViewById(R.id.backic)

        networkClient = NetworkClient(this)

        // Load current user profile from SharedPreferences
        loadUserProfile()

        saveButton.setOnClickListener {
            val updatedFirstName = firstNameEditText.text.toString().trim()
            val updatedLastName = lastNameEditText.text.toString().trim()
            val updatedEmail = emailEditText.text.toString().trim()
            val updatedContact = contactEditText.text.toString().trim()

            // Validate input
            if (updatedFirstName.isEmpty() || updatedLastName.isEmpty() || updatedEmail.isEmpty() || updatedContact.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create JSON object for updated data
            val payload = JSONObject()
            val userObject = JSONObject()
            try {
                userObject.put("first_name", updatedFirstName)
                userObject.put("last_name", updatedLastName)
                userObject.put("email", updatedEmail)
                payload.put("user", userObject)
                payload.put("contact", updatedContact)
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(this, "Error creating JSON data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = getUserId() // Get user ID from SharedPreferences
            updateProfile(userId, payload)
        }


        backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadUserProfile() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val firstName = sharedPreferences.getString(PREF_FIRST_NAME, "")
        val lastName = sharedPreferences.getString(PREF_LAST_NAME, "")
        val email = sharedPreferences.getString(PREF_EMAIL, "")
        val contact = sharedPreferences.getString(PREF_CONTACT, "")

        firstNameEditText.setText(firstName)
        lastNameEditText.setText(lastName)
        emailEditText.setText(email)
        contactEditText.setText(contact)
    }

    private fun getUserId(): String {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPreferences.getString(PREF_USER_ID, "") ?: ""
    }

    private fun updateProfile(userId: String, updatedData: JSONObject) {
        networkClient.updateUserProfile(userId, updatedData, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("UpdateProfile", "Response: $responseData")

                if (response.isSuccessful) {
                    try {
                        val updatedProfile = JSONObject(responseData)
                        val editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                        editor.putString(PREF_FIRST_NAME, updatedProfile.getJSONObject("user").getString("first_name"))
                        editor.putString(PREF_LAST_NAME, updatedProfile.getJSONObject("user").getString("last_name"))
                        editor.putString(PREF_EMAIL, updatedProfile.getJSONObject("user").getString("email"))
                        editor.putString(PREF_CONTACT, updatedProfile.getString("contact"))
                        editor.apply()

                        runOnUiThread {
                            Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            resultIntent.putExtra("updated_name", "${updatedProfile.getJSONObject("user").getString("first_name")} ${updatedProfile.getJSONObject("user").getString("last_name")}")
                            resultIntent.putExtra("updated_contact", updatedProfile.getString("contact"))
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@EditProfileActivity, "Error parsing server response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("UpdateProfile", "Error response: $responseData")
                }
            }
        })
    }
}
