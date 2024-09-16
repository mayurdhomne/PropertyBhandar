package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
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
        private const val PICK_IMAGE_REQUEST = 1
    }

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backIcon: ImageView
    private lateinit var uploadPhotoButton: Button
    private lateinit var editUserImageView: ImageView
    private lateinit var selectedImageUri: Uri
    private lateinit var networkClient: NetworkClient
    private var customLoadingDialog: CustomLoadingDialog? = null

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
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton)
        editUserImageView = findViewById(R.id.editUserImageView)

        networkClient = NetworkClient(this)

        // Load current user profile from SharedPreferences
        loadUserProfile()

        uploadPhotoButton.setOnClickListener {
            openImagePicker()
        }

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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            // Load the selected image into ImageView
            Picasso.get().load(selectedImageUri).into(editUserImageView)
        }
    }

    private fun loadUserProfile() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val firstName = sharedPreferences.getString(PREF_FIRST_NAME, "")
        val lastName = sharedPreferences.getString(PREF_LAST_NAME, "")
        val email = sharedPreferences.getString(PREF_EMAIL, "")
        val contact = sharedPreferences.getString(PREF_CONTACT, "")
        val imageUrl = sharedPreferences.getString("user_image_url", "") // Assuming you store the image URL in SharedPreferences

        firstNameEditText.setText(firstName)
        lastNameEditText.setText(lastName)
        emailEditText.setText(email)
        contactEditText.setText(contact)

        if (!imageUrl.isNullOrEmpty()) {
            // Load the image into the ImageView
            Picasso.get().load(imageUrl).into(editUserImageView)
        } else {
            // Handle case where there is no image
            editUserImageView.setImageResource(R.drawable.person) // Use a default image or placeholder
        }
    }


    private fun getUserId(): String {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return sharedPreferences.getString(PREF_USER_ID, "") ?: ""
    }

    private fun updateProfile(userId: String, updatedData: JSONObject) {
        showLoadingDialog()
        if (::selectedImageUri.isInitialized) {
            uploadProfileWithImage(userId, updatedData)
        } else {
            networkClient.updateUserProfile(userId, updatedData, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    hideLoadingDialog()
                    handleProfileUpdateResponse(response)
                }
            })
        }
    }

    private fun uploadProfileWithImage(userId: String, updatedData: JSONObject) {
        showLoadingDialog()
        val contentResolver = contentResolver
        val inputStream = contentResolver.openInputStream(selectedImageUri)
        val file = File.createTempFile("temp", ".jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        val imagePart = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user", updatedData.toString())
            .addFormDataPart("user_image", file.name, imagePart)
            .build()

        networkClient.uploadUserProfileWithImage(userId, requestBody, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    hideLoadingDialog()
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile with image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                hideLoadingDialog()
                if (response.isSuccessful) {
                    runOnUiThread {
                        handleProfileUpdateResponse(response)
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun handleProfileUpdateResponse(response: Response) {
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

                // Save the image URL
                val imageUrl = updatedProfile.getJSONObject("user").optString("profile_image_url", "")
                editor.putString("user_image_url", imageUrl)
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
