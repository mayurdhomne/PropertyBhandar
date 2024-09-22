package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class ProfileActivity : AppCompatActivity() {

    private val EDIT_PROFILE_REQUEST_CODE = 1

    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var greetingText: TextView
    private lateinit var editButton: TextView
    private lateinit var helpfulText: TextView
    private lateinit var logoutText: TextView
    private lateinit var settingIcon: ImageView
    private lateinit var networkClient: NetworkClient
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var backbutton: ImageView
    private var customLoadingDialog: CustomLoadingDialog? = null
    private lateinit var userImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)



        // Initialize views
        emailText = findViewById(R.id.email_text)
        phoneText = findViewById(R.id.phone_text)
        greetingText = findViewById(R.id.greeting_text)
        editButton = findViewById(R.id.editButton)
        helpfulText = findViewById(R.id.helpful_text)
        logoutText = findViewById(R.id.logout_text)
        settingIcon = findViewById(R.id.settingic)
        userImageView = findViewById(R.id.userImageView)

        // Initialize SharedPreferences and NetworkClient
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        networkClient = NetworkClient(this)

        backbutton = findViewById(R.id.backic)
        backbutton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Fetch user profile
        fetchUserProfile()

        // Set up listeners
        setListeners()
    }

    private fun setListeners() {
        editButton.setOnClickListener {
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            intent.putExtra("email", emailText.text.toString())
            intent.putExtra("contact", phoneText.text.toString())
            val names = greetingText.text.toString().split(" ")
            intent.putExtra("firstName", if (names.isNotEmpty()) names[0] else "")
            intent.putExtra("lastName", if (names.size > 1) names[1] else "")
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE)
        }

        findViewById<ImageView>(R.id.backic).setOnClickListener {
            onBackPressed()
        }

        // Set up the BottomNavigationView
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this@ProfileActivity, HomeActivity::class.java))
                    finish() // Close current activity to prevent back stack accumulation
                    true
                }
                R.id.navigation_property -> {
                    startActivity(Intent(this@ProfileActivity, PropertyListActivity::class.java))
                    finish() // Close current activity to prevent back stack accumulation
                    true
                }
                R.id.navigation_addproperty -> {
                    startActivity(Intent(this@ProfileActivity, AddPropertyFormActivity::class.java))
                    finish() // Close current activity to prevent back stack accumulation
                    true
                }
                R.id.navigation_profile -> {
                    // Already on ProfileActivity
                    true
                }
                else -> false
            }
        }

        // Set the correct item as selected in BottomNavigationView
        val profileItem = navView.menu.findItem(R.id.navigation_profile)
        profileItem.isChecked = true

        helpfulText.setOnClickListener {
            showBottomSheetDialog()
        }

        logoutText.setOnClickListener {
            showLogoutDialog()
        }

        settingIcon.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, SettingsActivity::class.java))
        }

        // Add listener for Contact Us TextView
        findViewById<TextView>(R.id.contactus).setOnClickListener {
            val intent = Intent(this@ProfileActivity, ContactUsActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Navigate to HomeActivity when back button is pressed
        val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Close current activity
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val buttonYes: LinearLayout = bottomSheetView.findViewById(R.id.yesButton)
        val buttonNo: LinearLayout = bottomSheetView.findViewById(R.id.noButton)
        val buttonRemindLater: TextView = bottomSheetView.findViewById(R.id.button_remind_later)

        buttonYes.setOnClickListener {
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        buttonNo.setOnClickListener {
            Toast.makeText(this, "Sorry to hear that! We'll try to improve.", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        buttonRemindLater.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showLogoutDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_logout, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val yesButton: CardView = bottomSheetView.findViewById(R.id.yesButton)
        val noButton: CardView = bottomSheetView.findViewById(R.id.noButton)

        yesButton.setOnClickListener {
            // Clear access token and refresh token using NetworkClient
            NetworkClient(this).logout(this)

            // Optional: Clear other user data in SharedPreferences if needed
            with(sharedPreferences.edit()) {
                // Remove other user-specific data if required
                apply()
            }

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to the Login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

            // Dismiss the dialog
            bottomSheetDialog.dismiss()
        }

        noButton.setOnClickListener {
            // Dismiss the dialog if user cancels logout
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun fetchUserProfile() {

        if (networkClient.getAccessToken() == null) {
            runOnUiThread {
                showLoginRequiredDialog()
            }
            return // Don't proceed with fetching the profile
        }

        showLoadingDialog()
        networkClient.fetchUserProfile(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                hideLoadingDialog()
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    // Show login required dialog when the profile fetch fails
                    showLoginRequiredDialog()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                hideLoadingDialog()
                try {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        val profileJson = JSONObject(responseData)
                        val userJson = profileJson.optJSONObject("user")
                        val email = userJson?.optString("email", "") ?: ""
                        val contact = profileJson.optString("contact", "")
                        val firstName = userJson?.optString("first_name", "") ?: ""
                        val lastName = userJson?.optString("last_name", "") ?: ""
                        val userImage = profileJson.optString("user_image", "")
                        val greetingMessage = getGreetingMessage(firstName)

                        // Store the user data in SharedPreferences
                        with(sharedPreferences.edit()) {
                            putString("firstName", firstName)
                            putString("lastName", lastName)
                            putString("email", email)
                            putString("contact", contact)
                            putString("user_image", userImage) // Save user image URL
                            putBoolean("isProfileFetched", true) // Store profile fetch status (Success)
                            apply()
                        }

                        runOnUiThread {
                            updateProfileData(email, contact, greetingMessage, userImage)
                        }
                    } else if (response.code == 401) { // Unauthorized
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Unauthorized access. Please log in again.", Toast.LENGTH_SHORT).show()
                            // Show login required dialog
                            showLoginRequiredDialog()
                            // Store profile fetch status in SharedPreferences (Failure)
                            with(sharedPreferences.edit()) {
                                putBoolean("isProfileFetched", false)
                                apply()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to fetch profile: ${response.code}", Toast.LENGTH_SHORT).show()
                            // Show login required dialog
                            showLoginRequiredDialog()
                            // Store profile fetch status in SharedPreferences (Failure)
                            with(sharedPreferences.edit()) {
                                putBoolean("isProfileFetched", false)
                                apply()
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Error reading response", Toast.LENGTH_SHORT).show()
                        // Show login required dialog
                        showLoginRequiredDialog()
                        // Store profile fetch status in SharedPreferences (Failure)
                        with(sharedPreferences.edit()) {
                            putBoolean("isProfileFetched", false)
                            apply()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                        // Show login required dialog
                        showLoginRequiredDialog()
                        // Store profile fetch status in SharedPreferences (Failure)
                        with(sharedPreferences.edit()) {
                            putBoolean("isProfileFetched", false)
                            apply()
                        }
                    }
                } finally {
                    response.body?.close()
                }
            }
        })
    }

    // Function to show login required dialog
    private fun showLoginRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_required, null) // Create a custom layout for the dialog
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        // Customize the dialog UI elements (using findViewById)
        val titleTextView: TextView = dialogView.findViewById(R.id.dialogTitle)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialogMessage)
        val loginButton: CardView = dialogView.findViewById(R.id.btnLoginNow)
        val cancelButton: CardView = dialogView.findViewById(R.id.btnMaybeLater)

        titleTextView.text = "Login Required"
        messageTextView.text = "You need to be logged in to access this feature."

        loginButton.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Optional: Finish the current activity if needed
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Customize the dialog appearance (optional)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background) // Set a custom background drawable

        dialog.show()
    }

    private fun updateProfileData(email: String, contact: String, greetingMessage: String, userImage: String) {
        emailText.text = email
        phoneText.text = contact
        greetingText.text = greetingMessage

        // Load user image into ImageView
        if (userImage.isNotEmpty()) {
            Glide.with(this)
                .load(userImage)
                .placeholder(R.drawable.person) // Replace with your placeholder image
                .error(R.drawable.person1) // Replace with your error image
                .into(userImageView)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchUserProfile()
        }
    }

    private fun getGreetingMessage(firstName: String?): String {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hourOfDay) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        return if (firstName.isNullOrEmpty()) {
            "$greeting!"
        } else {
            "$greeting, $firstName!"
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
