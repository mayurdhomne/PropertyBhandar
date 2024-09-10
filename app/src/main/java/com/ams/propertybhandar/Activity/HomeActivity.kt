package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Adaptar.LatestPropertyAdapter
import com.ams.propertybhandar.Adaptar.RecommandedProppertyAdapter
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionMenuDivider: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var latestRecyclerView: RecyclerView
    private lateinit var networkClient: NetworkClient
    private lateinit var searchEditText: EditText
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var navigationView: NavigationView
    private lateinit var addPropertyButton: MaterialButton
    private lateinit var addPropertyButton2: MaterialButton


    private lateinit var viewAllTextView1: TextView
    private lateinit var viewAllTextView2: TextView

    private var customLoadingDialog: CustomLoadingDialog? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize NetworkClient
        networkClient = NetworkClient(this)

        // Initialize ActivityResultLauncher
        editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Refresh user profile data
            }
        }

        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.buyflatbtn).setOnClickListener {
            navigateToPropertyList("Flat")
        }
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.buyplotbtn).setOnClickListener {
            navigateToPropertyList("Plot")
        }
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.buyshopbtn).setOnClickListener {
            navigateToPropertyList("Shop")
        }

        // Initialize DrawerLayout and actionMenuDivider
        drawerLayout = findViewById(R.id.drawer_layout)
        actionMenuDivider = findViewById(R.id.actionmenudivider)

        // Initialize NavigationView and locate views inside it
        navigationView = findViewById(R.id.nav_view2)
        val headerView = navigationView.getHeaderView(0)
        nameTextView = headerView.findViewById(R.id.nameTextView)
        emailTextView = headerView.findViewById(R.id.emailTextView)

        // Initialize "View All" TextViews
        viewAllTextView1 = findViewById(R.id.viewAllTextView1)
        viewAllTextView2 = findViewById(R.id.viewAllTextView2)

        // Initialize the AddPropertyButton
        addPropertyButton = findViewById(R.id.AddPropertyButton)
        addPropertyButton2 = findViewById(R.id.postPropertyButton)

        // Set up actionMenuDivider to open the drawer
        actionMenuDivider.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)  // Open the drawer from the end (right side)
        }

        // Fetch and display user's profile information

        // Set up the BottomNavigationView
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on HomeActivity
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                    finish() // Close current activity to prevent back stack accumulation
                    true
                }
                R.id.navigation_property -> {
                    startActivity(Intent(this@HomeActivity, PropertyListActivity::class.java))
                    finish() // Close current activity to prevent back stack accumulation
                    true
                }
                R.id.navigation_addproperty -> {
                    startActivity(Intent(this@HomeActivity, AddPropertyFormActivity::class.java))
                    finish() // Close current activity to prevent back stack accumulation
                    true
                }
                else -> false
            }
        }



        // Set the correct item as selected in BottomNavigationView
        val homeItem = navView.menu.findItem(R.id.navigation_home)
        homeItem.isChecked = true

        // Set up CardView click listeners
        findViewById<androidx.cardview.widget.CardView>(R.id.hbcalculator).setOnClickListener {
            val intent = Intent(this@HomeActivity, HomeBudgetCalculatorActivity::class.java)
            startActivity(intent)
        }
        findViewById<androidx.cardview.widget.CardView>(R.id.servicesbtn).setOnClickListener {
            val intent = Intent(this@HomeActivity, ServicesActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.buybtn).setOnClickListener {
            val intent = Intent(this@HomeActivity, BuyActivity::class.java)
            startActivity(intent)
        }
        findViewById<androidx.cardview.widget.CardView>(R.id.loanbtn).setOnClickListener {
            val intent = Intent(this@HomeActivity, LoanActivity::class.java)
            startActivity(intent)
        }

        // Set up the CardView click listeners
        findViewById<androidx.cardview.widget.CardView>(R.id.hbcalculator).setOnClickListener {
            val intent = Intent(this@HomeActivity, HomeBudgetCalculatorActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.lecalculator).setOnClickListener {
            val intent = Intent(this@HomeActivity, LoanEligibilityActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.emiCalculator).setOnClickListener {
            val intent = Intent(this@HomeActivity, EMICalculatorActivity::class.java)
            startActivity(intent)
        }

        // Newly added intents for additional calculators
        findViewById<androidx.cardview.widget.CardView>(R.id.emiCalculatorcard).setOnClickListener {
            val intent = Intent(this@HomeActivity, EMICalculatorActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.affordabilityCalculatorcard).setOnClickListener {
            val intent = Intent(this@HomeActivity, HomeBudgetCalculatorActivity::class.java)
            startActivity(intent)
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.hbcCalculatorcard).setOnClickListener {
            val intent = Intent(this@HomeActivity, LoanEligibilityActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recommendedrecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Initialize RecyclerView for latest properties
        latestRecyclerView = findViewById(R.id.latestrecyclerView)
        latestRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Fetch and display properties
        fetchProperties()




   // Handle NavigationView item clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile1 -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_Calculator -> {
                    startActivity(Intent(this, EMICalculatorActivity::class.java))
                    true
                }
                R.id.nav_Affordability -> {
                    startActivity(Intent(this, HomeBudgetCalculatorActivity::class.java))
                    true
                }
                R.id.nav_Eligibility -> {
                    startActivity(Intent(this, LoanEligibilityActivity::class.java))
                    true
                }
                R.id.nav_Property -> {
                    startActivity(Intent(this, PropertyListActivity::class.java))
                    true
                }
                R.id.nav_Loan -> {
                    startActivity(Intent(this, LoanApplicationActivity::class.java))
                    true
                }
                R.id.nav_contactus -> {
                    startActivity(Intent(this, ContactUsActivity::class.java))
                    true
                }
                R.id.nav_Setting -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_Log -> {
                    // Implement logout function
                    logout()
                    true
                }
                else -> false
            }
        }

        // Set up "View All" TextView click listeners
        viewAllTextView1.setOnClickListener {
            val intent = Intent(this@HomeActivity, PropertyListActivity::class.java)
            intent.putExtra("property_type", "recommended") // Pass type to filter properties
            startActivity(intent)
        }

        viewAllTextView2.setOnClickListener {
            val intent = Intent(this@HomeActivity, PropertyListActivity::class.java)
            intent.putExtra("property_type", "latest") // Pass type to filter properties
            startActivity(intent)
        }
        // Handle "Add Property" button click
// Set up the AddPropertyButton click listener
        addPropertyButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, AddPropertyFormActivity::class.java)
            startActivity(intent)
        }
        addPropertyButton2.setOnClickListener {
            val intent = Intent(this@HomeActivity, AddPropertyFormActivity::class.java)
            startActivity(intent)
        }
        searchEditText = findViewById(R.id.searchView)
        // Use this method in HomeActivity to handle search actions
        // Handle search actions
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = v.text.toString()
                if (query.isNotEmpty()) {
                    handleSearch(query) // Trigger search in PropertyListActivity
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        // Inflate the custom dialog layout
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_exit_confirmation, null)

        // Set up the dialog with custom layout
        builder.setView(dialogLayout)

        // Find buttons in the custom layout
        val stayButton = dialogLayout.findViewById<MaterialButton>(R.id.stayButton)
        val exitButton = dialogLayout.findViewById<MaterialButton>(R.id.exitButton)

        // Create the dialog and show it
        val dialog = builder.create()
        dialog.show()

        // Handle "Stay" button click
        stayButton.setOnClickListener {
            dialog.dismiss() // Close the dialog and stay in the app
        }

        // Handle "Exit" button click
        exitButton.setOnClickListener {
            finishAffinity() // Close the app
            dialog.dismiss()
        }
    }


    private fun handleSearch(query: String) {
        val intent = Intent(this, PropertyListActivity::class.java)
        intent.putExtra("keywords", query)
        startActivity(intent)
    }

    private fun fetchProperties() {
        showLoadingDialog()
        networkClient.fetchProperties(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Failed to fetch properties", Toast.LENGTH_SHORT).show()
                    logoutAndRedirectToLogin()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    hideLoadingDialog()
                    response.body?.let { responseBody ->
                        try {
                            val jsonArray = JSONArray(responseBody.string())
                            runOnUiThread {
                                // Set up the adapter for recommended properties
                                val recommendedAdapter = RecommandedProppertyAdapter(this@HomeActivity, jsonArray)
                                recyclerView.adapter = recommendedAdapter

                                // Set up the adapter for latest properties
                                val latestAdapter = LatestPropertyAdapter(this@HomeActivity, jsonArray)
                                latestRecyclerView.adapter = latestAdapter
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@HomeActivity, "Failed to parse properties", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        if (response.code == 401) {
                            // Session expired, clear session data and redirect to login
                            Toast.makeText(this@HomeActivity, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
                            logoutAndRedirectToLogin()
                        } else {
                            val errorMessage = "Error: ${response.message}"
                            Toast.makeText(this@HomeActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    private fun logoutAndRedirectToLogin() {
        // Clear user session data
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect to login or splash screen
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish() // Close current activity to prevent back stack accumulation
    }


    private fun logout() {
        // Clear user session data
        val sharedPreferences: SharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect to login or splash screen
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish() // Close current activity to prevent back stack accumulation
    }
    private fun navigateToPropertyList(propertyType: String) {
        val intent = Intent(this, PropertyListActivity::class.java).apply {
            putExtra("search_query", propertyType)
        }
        startActivity(intent)
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
