package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Adaptar.LatestPropertyAdapter
import com.ams.propertybhandar.Adaptar.RecommandedProppertyAdapter
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
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
    private lateinit var propertyImageView: ShapeableImageView
    private lateinit var calculatorImageView: ImageView
    private lateinit var calculatorImageView2: ImageView
    private lateinit var calculatorImageView3: ImageView
    private lateinit var buyHomeImageView: ImageView
    private lateinit var buyPlotImageView: ImageView
    private lateinit var buyShopImageView: ImageView
    private lateinit var viewAllTextView1: TextView
    private lateinit var viewAllTextView2: TextView
    private var customLoadingDialog: CustomLoadingDialog? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        propertyImageView = findViewById(R.id.propertyImageView)
        calculatorImageView = findViewById(R.id.calculator_image)
        calculatorImageView2 = findViewById(R.id.calculator_image2)
        calculatorImageView3 = findViewById(R.id.calculator_image3)
        buyHomeImageView = findViewById(R.id.buyHomeImageView)
        buyPlotImageView = findViewById(R.id.rentHomeImageView)
        buyShopImageView = findViewById(R.id.pgCoLivingImageView)
        val imageUrl = "https://drive.google.com/uc?export=download&id=1BIoT5aBuLzqRnqaQyy6WIMUxIs8hGYO3"
        Picasso.get()
            .load(imageUrl)
            .fit()
            .centerCrop()
            .into(propertyImageView)
        // Direct download link for the first calculator image
        val calculatorImageUrl = "https://drive.google.com/uc?export=download&id=18g4u7uLEEhuoUSZVnwqMHLLGGkHaC_6L"
        Picasso.get()
            .load(calculatorImageUrl)
            .fit()
            .centerCrop()
            .into(calculatorImageView)
        // Direct download link for the second calculator image
        val calculatorImageUrl2 = "https://drive.google.com/uc?export=download&id=1vKBL78422AJQJBXy8Lhm1DfADG9em8JZ"
        Picasso.get()
            .load(calculatorImageUrl2)
            .fit()
            .centerCrop()
            .into(calculatorImageView2)
        // Direct download link for the third calculator image
        val calculatorImageUrl3 = "https://drive.google.com/uc?export=download&id=1nNrBU6LEOOYA1e1ztB3qi-fNHUoNYRsW"
        Picasso.get()
            .load(calculatorImageUrl3)
            .fit()
            .centerCrop()
            .into(calculatorImageView3)
        // Direct download link for the first buy home image
        val buyHomeImageUrl = "https://drive.google.com/uc?export=download&id=1XuqBeomGtC-5ZIYkxB42tVwARlapF1CH"
        Picasso.get()
            .load(buyHomeImageUrl)
            .fit()
            .centerCrop()
            .into(buyHomeImageView)
        // Direct download link for the second buy home image
        val buyPlotImageUrl = "https://drive.google.com/uc?export=download&id=1vPbMb9MaQRlMz5eCJy2pJBM4U6YV4U1f"
        Picasso.get()
            .load(buyPlotImageUrl)
            .fit()
            .centerCrop()
            .into(buyPlotImageView)
        // Direct download link for the third buy home image
        val buyShopImageUrl = "https://drive.google.com/uc?export=download&id=1iRvf4_9wnLrG-93eRMoxic0ulIRzPbgU"
        Picasso.get()
            .load(buyShopImageUrl)
            .fit()
            .centerCrop()
            .into(buyShopImageView)
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
        // Initalize "View All" TextViews
        viewAllTextView1 = findViewById(R.id.viewAllTextView1)
        viewAllTextView2 = findViewById(R.id.viewAllTextView2)
        // Initialize the AddPropertyButton
        addPropertyButton = findViewById(R.id.AddPropertyButton)
        addPropertyButton2 = findViewById(R.id.postPropertyButton)
        // Set up actionMenuDivider to open the drawer
        actionMenuDivider.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)  // Open the drawer from the end (right side)
        }
        // Fetch an display user's profile information


// Set up the BottomNavigationView
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Already on HomeActivity
                    true
                }
                R.id.navigation_profile -> {
                    if (networkClient.getAccessToken() == null) {
                        showLoginRequiredDialog()
                    } else {
                        startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.navigation_property -> {
                    // No login check for property list; assuming it's public
                    startActivity(Intent(this@HomeActivity, PropertyListActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_addproperty -> {
                    if (networkClient.getAccessToken() == null) {
                        showLoginRequiredDialog()
                    } else {
                        startActivity(Intent(this@HomeActivity, AddPropertyFormActivity::class.java))
                        finish()
                    }
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
        // Find your RecyclerView
        recyclerView = findViewById(R.id.recommendedrecyclerView)

// Set the LayoutManager for horizontal scrolling
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

// Use DefaultItemAnimator for custom fade, scale, and move animations
        recyclerView.itemAnimator = object : DefaultItemAnimator() {

            override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
                holder?.itemView?.apply {
                    // Starting state for animation (fade, scale, translation)
                    alpha = 0f
                    scaleX = 0.8f
                    scaleY = 0.8f
                    translationY = 200f // start slightly lower
                    rotation = 10f

                    // Animate the view to final state
                    animate()
                        .alpha(1f) // fade in
                        .scaleX(1f) // scale up
                        .scaleY(1f) // scale up
                        .translationY(0f) // move to the final position
                        .rotation(0f) // rotate back to normal
                        .setDuration(500) // set animation duration
                        .setInterpolator(DecelerateInterpolator()) // smooth animation
                        .start()
                }
                return super.animateAdd(holder)
            }

            override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
                holder?.itemView?.apply {
                    // Starting state for removal animation
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                    translationX = 0f
                    rotation = 0f

                    // Animate the view to exit state
                    animate()
                        .alpha(0f) // fade out
                        .scaleX(0.5f) // shrink the view
                        .scaleY(0.5f) // shrink the view
                        .translationX(-200f) // slide out to the left
                        .rotation(20f) // slight rotation on removal
                        .setDuration(400) // set animation duration
                        .setInterpolator(AccelerateInterpolator()) // faster exit
                        .start()
                }
                return super.animateRemove(holder)
            }

            override fun animateMove(
                holder: RecyclerView.ViewHolder?,
                fromX: Int,
                fromY: Int,
                toX: Int,
                toY: Int
            ): Boolean {
                holder?.itemView?.apply {
                    // Animation for moving an item
                    animate()
                        .translationX(toX.toFloat()) // horizontal translation
                        .setDuration(300)
                        .setInterpolator(LinearInterpolator()) // constant speed
                        .start()
                }
                return super.animateMove(holder, fromX, fromY, toX, toY)
            }
        }
        // Initialize RecyclerView for latest properties
        latestRecyclerView = findViewById(R.id.latestrecyclerView)
        latestRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Fetch and display properties
        fetchProperties()
   // Handle NavigationView item clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile1 -> {
                    if (networkClient.getAccessToken() == null) {
                        showLoginRequiredDialog() // Call the dialog function
                    } else {
                        startActivity(Intent(this, ProfileActivity::class.java))

                    }
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
                    if (networkClient.getAccessToken() == null) {
                        showLoginRequiredDialog() // Call the dialog function
                    } else {
                        startActivity(Intent(this, LoanApplicationActivity::class.java))
                    }
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
        // Listener for addPropertyButton
        // Listener for addPropertyButton
        addPropertyButton.setOnClickListener {
            if (networkClient.getAccessToken() == null) {
                showLoginRequiredDialog()
            } else {
                val intent = Intent(this@HomeActivity, AddPropertyFormActivity::class.java)
                startActivity(intent)
            }
        }
        addPropertyButton2.setOnClickListener {
            if (networkClient.getAccessToken() == null) {
                showLoginRequiredDialog()
            } else {
                val intent = Intent(this@HomeActivity, LoanApplicationActivity::class.java)
                startActivity(intent)
            }
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

    private fun showLoginRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_required, null) // Assuming you have a dialog_login_required.xml layout
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

        dialog.show()
    }



    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
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
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                     hideLoadingDialog()
                    response.body?.let { responseBody ->
                        try {
                            val jsonArray = JSONArray(response.body?.string())
                            val limit = 6

                            // Limit the number of properties
                            val recommendedJsonArray = JSONArray()
                            val latestJsonArray = JSONArray()

                            for (i in 0 until jsonArray?.length()?.let { minOf(it, limit) }!!
                            ) {
                                recommendedJsonArray.put(jsonArray?.getJSONObject(i))
                            }

                            for (i in jsonArray?.length()!! - 1 downTo maxOf(jsonArray?.length()!! - limit, 0)) {
                                latestJsonArray.put(jsonArray?.getJSONObject(i))
                            }

                            runOnUiThread {
                                // Set up the adapter for recommended properties (last to first)
                                val recommendedAdapter = RecommandedProppertyAdapter(this@HomeActivity, recommendedJsonArray)
                                recyclerView.adapter = recommendedAdapter

                                // Set up the adapter for latest properties (first to last)
                                val latestAdapter = LatestPropertyAdapter(this@HomeActivity, latestJsonArray)
                                latestRecyclerView.adapter = latestAdapter
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@HomeActivity, "Failed to parse properties", Toast.LENGTH_SHORT).show()
                                e.printStackTrace() // Print the stack trace to debug
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "Failed to fetch properties", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        })
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
