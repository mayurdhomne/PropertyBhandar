package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
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
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            bottomSheetDialog.dismiss()
        }

        noButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun fetchUserProfile() {
        networkClient.fetchUserProfile(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        val profileJson = JSONObject(responseData)
                        val userJson = profileJson.optJSONObject("user")
                        val email = userJson?.optString("email", "") ?: ""
                        val contact = profileJson.optString("contact", "")
                        val firstName = userJson?.optString("first_name", "") ?: ""
                        val lastName = userJson?.optString("last_name", "") ?: ""
                        val greetingMessage = getGreetingMessage(firstName)

                        with(sharedPreferences.edit()) {
                            putString("firstName", firstName)
                            putString("lastName", lastName)
                            putString("email", email)
                            putString("contact", contact)
                            apply()
                        }

                        runOnUiThread {
                            updateProfileData(email, contact, greetingMessage)
                        }
                    } else if (response.code == 401) {
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Unauthorized access. Please log in again.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ProfileActivity, "Failed to fetch profile: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Error reading response", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@ProfileActivity, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    response.body?.close()
                }
            }
        })
    }

    private fun updateProfileData(email: String, contact: String, greetingMessage: String) {
        emailText.text = email
        phoneText.text = contact
        greetingText.text = greetingMessage
    }

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
}
