package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.ams.propertybhandar.Adaptar.PhotoPagerAdapter
import com.ams.propertybhandar.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject

class PropertyDetailsActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)

        // Retrieve the property data from the Intent
        val propertyData = intent.getStringExtra("property_data")
        val property = JSONObject(propertyData.toString())

        // Bind the UI elements to variables
        val photoViewPager: ViewPager2 = findViewById(R.id.photo_view_pager)
        val photoTabLayout: TabLayout = findViewById(R.id.photo_tab_layout)
        val title: TextView = findViewById(R.id.title)
        val price: TextView = findViewById(R.id.price)
        val address: TextView = findViewById(R.id.address)
        val state: TextView = findViewById(R.id.state)
        val zipcode: TextView = findViewById(R.id.zipcode)
        val property_facing: TextView = findViewById(R.id.property_facing)
        val type: TextView = findViewById(R.id.type)
        val area: TextView = findViewById(R.id.area)
        val bed: TextView = findViewById(R.id.bed)
        val bath: TextView = findViewById(R.id.bath)
        val description: TextView = findViewById(R.id.descripction)
        val amenities1: TextView = findViewById(R.id.amenities1)
        val amenities2: TextView = findViewById(R.id.amenities2)
        val amenities3: TextView = findViewById(R.id.amenities3)
        val amenities4: TextView = findViewById(R.id.amenities4)
        val amenities5: TextView = findViewById(R.id.amenities5)
        val amenities6: TextView = findViewById(R.id.amenities6)
        val buyButton: Button = findViewById(R.id.check_availability_button)

        // Set the values from the property object
        // Set the values from the property object
        title.text = property.optString("title", "N/A")
        price.text = property.optString("price", "N/A")
        address.text = property.optString("address", "N/A")
        area.text = "${property.optString("area", "N/A")} Sq.Ft"
        type.text = property.optString("property_type", "N/A")
        property_facing.text = property.optString("property_facing", "N/A")
        bed.text = property.optString("beds", "N/A")
        bath.text = property.optString("baths", "N/A")
        state.text = "${property.optString("state", "N/A")}, ${property.optString("city", "N/A")}"
        zipcode.text = property.optString("zipcode", "N/A")
        description.text = property.optString("description", "N/A")
        amenities1.text = property.optString("amenities1", "N/A")
        amenities2.text = property.optString("amenities2", "N/A")
        amenities3.text = property.optString("amenities3", "N/A")
        amenities4.text = property.optString("amenities4", "N/A")
        amenities5.text = property.optString("amenities5", "N/A")
        amenities6.text = property.optString("amenities6", "N/A")


        // Prepare the list of photo URLs with dynamic URL handling
        val baseUrl = "https://propertybhandar.com"
        val photoUrls = listOfNotNull(
            getFullImageUrl(property.optString("photo_main"), baseUrl),
            getFullImageUrl(property.optString("photo_1"), baseUrl),
            getFullImageUrl(property.optString("photo_2"), baseUrl),
            getFullImageUrl(property.optString("photo_3"), baseUrl),
            getFullImageUrl(property.optString("photo_4"), baseUrl)
        )

        // Set up the ViewPager and TabLayout only if there are photos
        if (photoUrls.isNotEmpty()) {
            val adapter = PhotoPagerAdapter(this, photoUrls)
            photoViewPager.adapter = adapter

            // Set up the TabLayout with dots indicators
            TabLayoutMediator(photoTabLayout, photoViewPager) { tab, position ->
                tab.icon = ContextCompat.getDrawable(this, R.drawable.custom_dot_unselected)
            }.attach()

            photoViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    for (i in 0 until photoTabLayout.tabCount) {
                        photoTabLayout.getTabAt(i)?.icon = ContextCompat.getDrawable(this@PropertyDetailsActivity, R.drawable.custom_dot_unselected)
                    }
                    photoTabLayout.getTabAt(position)?.icon = ContextCompat.getDrawable(this@PropertyDetailsActivity, R.drawable.custom_dot_selected)
                }
            })

            // Show the photo-related views
            photoViewPager.visibility = View.VISIBLE
            photoTabLayout.visibility = View.VISIBLE
        } else {
            // Hide the TabLayout and ViewPager if there are no photos
            photoTabLayout.visibility = View.GONE
            photoViewPager.visibility = View.GONE
        }

        // Handle the back button click
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Handle the buy property button click
        buyButton.setOnClickListener {
            // Navigate to ContactUsActivity
            val intent = Intent(this, ContactUsActivity::class.java)
            // Optionally, pass property details to the next activity if needed
            intent.putExtra("property_data", propertyData)
            startActivity(intent)
        }
    }

    // Helper function to handle dynamic image URL construction
    private fun getFullImageUrl(imageUrl: String?, baseUrl: String): String? {
        return if (imageUrl != null && imageUrl.isNotEmpty()) {
            if (imageUrl.startsWith("http")) {
                imageUrl // It's a complete URL
            } else {
                baseUrl + imageUrl // Prepend the base URL for relative URLs
            }
        } else {
            null
        }
    }
}
