package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Adaptar.PropertyListAdapter
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

@Suppress("DEPRECATION")
class PropertyListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var propertyListAdapter: PropertyListAdapter
    private var allProperties: JSONArray = JSONArray() // Initialize with an empty JSONArray
    private var searchPerformed: Boolean = false // Track if a search was performed
    private var customLoadingDialog: CustomLoadingDialog? = null
    private var minBudget: Double = 0.0
    private var maxBudget: Double = Double.MAX_VALUE
    private var selectedPropertyType: String = "All"




    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_property_list)

        val searchQuery = intent.getStringExtra("keywords") ?: ""
        minBudget = intent.getDoubleExtra("min_budget", 0.0)
        maxBudget = intent.getDoubleExtra("max_budget", Double.MAX_VALUE)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.propertyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        // Initialize adapter with click listener and wishlist toggle listener
        propertyListAdapter = PropertyListAdapter(this, allProperties, { property ->
            val intent = Intent(this@PropertyListActivity, PropertyDetailsActivity::class.java).apply {
                putExtra("property_data", property.toString()) // Pass property data
            }
            startActivity(intent)
        }, { property, isChecked ->
        })
        recyclerView.adapter = propertyListAdapter

        // Automatically trigger the search or fetch all properties based on intent extras
        if (searchQuery.isNotEmpty()) {
            searchPerformed = true
            fetchProperties(searchQuery) // Removed minBudget and maxBudget since they're not used here
        } else {
            searchPerformed = false
            fetchAllProperties()  // Fetch all properties when no search query is provided
        }

        // Set up the BottomNavigationView
        setupBottomNavigation()

        // Back icon click listener
        findViewById<ImageView>(R.id.backic).setOnClickListener {
            navigateToHome()
        }

        // Set up filter icon click listener
        findViewById<ImageView>(R.id.filteric).setOnClickListener { view ->
            showFilterMenu(view)
        }

        // Set up card click listeners for filtering
        setupCardFilters()
    }
    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_property -> true // Already on PropertyListActivity
                R.id.navigation_profile -> {
                    if (isLoggedIn()) {
                        // User is logged in, navigate to ProfileActivity
                        startActivity(Intent(this@PropertyListActivity, ProfileActivity::class.java))
                        finish()
                    } else {
                        // Show a dialog instead of Toast to notify the user to log in
                        showLoginDialog("You need to log in to view your profile.")
                    }
                    true
                }
                R.id.navigation_home -> {
                    startActivity(Intent(this@PropertyListActivity, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_addproperty -> {
                    if (isLoggedIn()) {
                        // User is logged in, navigate to AddPropertyFormActivity
                        startActivity(Intent(this@PropertyListActivity, AddPropertyFormActivity::class.java))
                        finish()
                    } else {
                        // Show a dialog instead of Toast to notify the user to log in
                        showLoginDialog("You need to log in to add a property.")
                    }
                    true
                }
                else -> false
            }
        }
        // Set the correct item as selected in BottomNavigationView
        navView.menu.findItem(R.id.navigation_property).isChecked = true
    }
    private fun showLoginDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login Required")
        builder.setMessage(message)
        builder.setIcon(R.drawable.ic_warning) // Optional: Add an icon to make it look professional

        builder.setPositiveButton("Log in Now") { dialog, _ ->
            // Navigate to LoginActivity
            val loginIntent = Intent(this@PropertyListActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            dialog.dismiss()
            finish() // Optional: Close HomeActivity after navigating to LoginActivity
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Just dismiss the dialog
            dialog.dismiss()
        }

        // Create and show the dialog
        val dialog = builder.create()

        // Set custom background if needed
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        dialog.show()
    }
    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false) // Adjust based on how login status is stored
    }

    private fun navigateToHome() {
        val intent = Intent(this@PropertyListActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("NewApi")
    private fun setupCardFilters() {
        // Define the colors for default and selected states
        val defaultColor = resources.getColor(R.color.backgroundColor, null)
        val selectedColor = resources.getColor(R.color.backgroundColor2, null)

        // Find CardViews for filtering
        val allPropertyCard = findViewById<CardView>(R.id.allpropertycard)
        val flatsCard = findViewById<CardView>(R.id.flatscard)
        val plotsCard = findViewById<CardView>(R.id.plotscard)
        val commercialsCard = findViewById<CardView>(R.id.comercialscard)
        val housesCard = findViewById<CardView>(R.id.Housescard)
        val apartmentsCard = findViewById<CardView>(R.id.Apartmentcard)

        // Function to reset all card backgrounds to default and highlight the selected card
        fun setSelectedCard(selectedCard: CardView, type: String) {
            allPropertyCard.setCardBackgroundColor(defaultColor)
            flatsCard.setCardBackgroundColor(defaultColor)
            plotsCard.setCardBackgroundColor(defaultColor)
            commercialsCard.setCardBackgroundColor(defaultColor)
            housesCard.setCardBackgroundColor(defaultColor)
            apartmentsCard.setCardBackgroundColor(defaultColor)
            selectedCard.setCardBackgroundColor(selectedColor)

            // Set the selected property type
            selectedPropertyType = type
        }

// Update card click listeners
        allPropertyCard.setOnClickListener {
            filterProperties("All", minBudget, maxBudget)
            setSelectedCard(allPropertyCard, "All")
        }

        flatsCard.setOnClickListener {
            filterProperties("Flat", minBudget, maxBudget)
            setSelectedCard(flatsCard, "Flat")
        }

        plotsCard.setOnClickListener {
            filterProperties("Plot", minBudget, maxBudget)
            setSelectedCard(plotsCard, "Plot")
        }

        commercialsCard.setOnClickListener {
            filterProperties("Shop", minBudget, maxBudget)
            setSelectedCard(commercialsCard, "Shop")
        }

        housesCard.setOnClickListener {
            filterProperties("House", minBudget, maxBudget)
            setSelectedCard(housesCard, "House")
        }

        apartmentsCard.setOnClickListener {
            filterProperties("Apartment", minBudget, maxBudget)
            setSelectedCard(apartmentsCard, "Apartment")
        }
    }
    // Method to fetch all properties and filter them based on budget range
    private fun fetchAllProperties() {
        showLoadingDialog()
        val networkClient = NetworkClient(this)
        networkClient.fetchProperties(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("PropertyFetch", "Failed to fetch properties: ${e.message}")
                    Toast.makeText(this@PropertyListActivity, "Failed to fetch properties", Toast.LENGTH_SHORT).show()
                    hideLoadingDialog()
                }
            }

            // Declare the flag at the class level
            private var toastShown = false

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("PropertyFetch", "Response: $responseBody")
                    runOnUiThread {
                        try {
                            if (!responseBody.isNullOrEmpty()) {
                                allProperties = JSONArray(responseBody)
                                // Call filterProperties with budget range
                                filterProperties("All", minBudget, maxBudget)
                                toastShown = false // Reset the flag since properties are found
                            } else {
                                Log.d("PropertyFetch", "No properties found")
                                // Show toast only if it hasn't been shown yet
                                if (!toastShown) {
                                    Toast.makeText(this@PropertyListActivity, "No properties found", Toast.LENGTH_SHORT).show()
                                    toastShown = true // Set the flag to prevent multiple toasts
                                }
                                hideLoadingDialog()
                            }
                        } catch (e: Exception) {
                            Log.e("PropertyFetch", "Error parsing properties: ${e.message}")
                            Toast.makeText(this@PropertyListActivity, "Error parsing properties", Toast.LENGTH_SHORT).show()
                        } finally {
                            hideLoadingDialog()
                        }
                    }
                } else {
                    Log.e("PropertyFetch", "Error fetching properties: ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@PropertyListActivity, "Error fetching properties: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    hideLoadingDialog()
                }
            }


        })
    }
    // Method to search properties based on keywords and filter them based on budget range
    private fun fetchProperties(keywords: String) {
        showLoadingDialog()
        val networkClient = NetworkClient(this)
        networkClient.searchProperties(keywords, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("PropertySearch", "Failed to search properties: ${e.message}")
                    Toast.makeText(this@PropertyListActivity, "Failed to search properties", Toast.LENGTH_SHORT).show()
                    hideLoadingDialog()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("PropertySearch", "Response: $responseBody")
                    runOnUiThread {
                        try {
                            if (!responseBody.isNullOrEmpty()) {
                                allProperties = JSONArray(responseBody)
                                // Call filterProperties with budget range
                                filterProperties("All", minBudget, maxBudget)
                            } else {
                                Log.d("PropertySearch", "No properties found")
                                Toast.makeText(this@PropertyListActivity, "No properties found", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("PropertySearch", "Error parsing search results: ${e.message}")
                            Toast.makeText(this@PropertyListActivity, "Error parsing search results", Toast.LENGTH_SHORT).show()
                        } finally {
                            hideLoadingDialog()
                        }
                    }
                } else {
                    Log.e("PropertySearch", "Error fetching search results: ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@PropertyListActivity, "Error fetching search results: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    hideLoadingDialog()
                }
            }
        })
    }

    // Method to filter properties based on type and budget range
    private fun filterProperties(type: String, minBudget: Double, maxBudget: Double) {
        val filteredProperties = JSONArray()
        for (i in 0 until allProperties.length()) {
            val property = allProperties.getJSONObject(i)
            val price = property.optDouble("price", 0.0)
            val propertyType = property.optString("property_type")

            // Filter properties based on type and budget range
            if ((type == "All" || propertyType == type) && price in minBudget..maxBudget) {
                filteredProperties.put(property)
            }
        }

        // Check if any properties were found AFTER the loop
        if (filteredProperties.length() == 0) {
            Toast.makeText(this@PropertyListActivity, "No properties found", Toast.LENGTH_SHORT).show()
        }

        // Update adapter with filtered properties
        propertyListAdapter.updateProperties(filteredProperties)

        hideLoadingDialog()
    }
    private fun showFilterMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu)
        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menuPopupHelper = popup.get(popupMenu)
            val popupWindow = menuPopupHelper.javaClass.getDeclaredField("mPopup")
            popupWindow.isAccessible = true
            val listView = popupWindow.get(menuPopupHelper) as ListView

            // Set the background color of the list view
            listView.setBackgroundColor(Color.WHITE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_low_to_high -> {
                    sortPropertiesByPrice(ascending = true, selectedPropertyType)
                    true
                }
                R.id.sort_high_to_low -> {
                    sortPropertiesByPrice(ascending = false, selectedPropertyType)
                    true
                }
                R.id.east_to_west -> {
                    filterPropertiesByDirection("East", selectedPropertyType)
                    true
                }
                R.id.west_to_east -> {
                    filterPropertiesByDirection("West", selectedPropertyType)
                    true
                }
                R.id.north_to_south -> {
                    filterPropertiesByDirection("North", selectedPropertyType)
                    true
                }
                R.id.south_to_north -> {
                    filterPropertiesByDirection("South", selectedPropertyType)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }


    private fun sortPropertiesByPrice(ascending: Boolean, type: String) {
        val filteredProperties = JSONArray()
        for (i in 0 until allProperties.length()) {
            val property = allProperties.getJSONObject(i)
            val propertyType = property.optString("property_type")

            // Filter by selected type
            if (type == "All" || propertyType == type) {
                filteredProperties.put(property)
            }
        }

        val propertyList = mutableListOf<JSONObject>()
        for (i in 0 until filteredProperties.length()) {
            propertyList.add(filteredProperties.getJSONObject(i))
        }

        // Sort by price
        propertyList.sortWith(compareBy { it.optDouble("price", 0.0) })

        if (!ascending) {
            propertyList.reverse()
        }

        val sortedProperties = JSONArray(propertyList)
        propertyListAdapter.updateProperties(sortedProperties)
    }


    private fun filterPropertiesByDirection(direction: String, type: String) {
        val filteredProperties = JSONArray()
        for (i in 0 until allProperties.length()) {
            val property = allProperties.getJSONObject(i)
            val propertyDirection = property.optString("property_facing", "")
            val propertyType = property.optString("property_type")

            // Filter by type and direction
            if (propertyDirection.equals(direction, ignoreCase = true) &&
                (type == "All" || propertyType == type)) {
                filteredProperties.put(property)
            }
        }

        propertyListAdapter.updateProperties(filteredProperties)
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
