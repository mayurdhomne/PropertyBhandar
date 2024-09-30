package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.ams.propertybhandar.Adaptar.PhotoPagerAdapter
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("UNUSED_EXPRESSION")
class PropertyDetailsActivity : AppCompatActivity() {

    private lateinit var networkClient: NetworkClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_details)



        // Retrieve the property data from the Intent
        val propertyData = intent.getStringExtra("property_data")
        val property = JSONObject(propertyData.toString())

        networkClient = NetworkClient(this)


        // Set up the back button
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }

        findViewById<ImageView>(R.id.share_button).setOnClickListener {
            sharePropertyDetails(property)
        }


        // Bind the UI elements to variables
        val photoViewPager: ViewPager2 = findViewById(R.id.photo_view_pager)
        val photoTabLayout: TabLayout = findViewById(R.id.photo_tab_layout)
        val title: TextView = findViewById(R.id.title)
        val price: TextView = findViewById(R.id.price)
        val address: TextView = findViewById(R.id.address)
        val state: TextView = findViewById(R.id.state)
        val zipcode: TextView = findViewById(R.id.zipcode)
        val property_facing: TextView = findViewById(R.id.property_facing)
        val updated_at: TextView = findViewById(R.id.updated_at)
        val id: TextView = findViewById(R.id.property_id)
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
        val updatedAt = property.optString("updated_at", "")


        // Set the values from the property object with visibility checks
        setTextViewVisibility(title, property.optString("title", ""))
        setTextViewVisibility(price, "₹${property.getString("price")}")
        setTextViewVisibility(address, property.optString("address", ""))
        setTextViewWithLabel(area, "Area:", property.optString("area", "N/A") + " Sq.Ft")
        setTextViewWithLabel(type, "Property Type:", property.optString("property_type", ""))
        setTextViewWithLabel(property_facing,"Property Facing:", property.optString("property_facing", ""))
        setTextViewWithLabel(updated_at, "Updated At:", formatUpdatedAt(updatedAt))
        setTextViewWithLabel(id, "Property ID:", property.optString("id", ""))
        setTextViewVisibility(bed, property.optString("beds", ""))
        setTextViewVisibility(bath, property.optString("baths", ""))
        setTextViewVisibility(state, "${property.optString("state", "")}, ${property.optString("city", "")}")
        setTextViewVisibility(zipcode, property.optString("zipcode", ""))
        setTextViewVisibility(description, property.optString("description", ""))
        setTextViewVisibilityWithStar(amenities1, property.optString("amenities1", ""))
        setTextViewVisibilityWithStar(amenities2, property.optString("amenities2", ""))
        setTextViewVisibilityWithStar(amenities3, property.optString("amenities3", ""))
        setTextViewVisibilityWithStar(amenities4, property.optString("amenities4", ""))
        setTextViewVisibilityWithStar(amenities5, property.optString("amenities5", ""))
        setTextViewVisibilityWithStar(amenities6, property.optString("amenities6", ""))

        // Prepare the list of photo URLs
        val baseUrl = "https://propertybhandar.com"
        val photoUrls = listOfNotNull(
            getFullImageUrl(property.optString("photo_main"), baseUrl),
            getFullImageUrl(property.optString("photo_1"), baseUrl),
            getFullImageUrl(property.optString("photo_2"), baseUrl),
            getFullImageUrl(property.optString("photo_3"), baseUrl),
            getFullImageUrl(property.optString("photo_4"), baseUrl)
        )

        // Set up the ViewPager and TabLayout
        if (photoUrls.isNotEmpty()) {
            val adapter = PhotoPagerAdapter(this, photoUrls, photoViewPager)
            photoViewPager.adapter = adapter

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

            photoViewPager.visibility = View.VISIBLE
            photoTabLayout.visibility = View.VISIBLE
        } else {
            photoTabLayout.visibility = View.GONE
            photoViewPager.visibility = View.GONE
        }

        // Handle the back button click
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Handle the buy property button click
        buyButton.setOnClickListener {
            if (networkClient.getAccessToken() == null) {
                showLoginRequiredDialog()
            } else {
                val intent = Intent(this, ContactUsActivity::class.java)
                intent.putExtra("property_data", propertyData)
                startActivity(intent)
            }
        }
    }
    private fun formatUpdatedAt(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(dateString.replace("Z", "+0000")) // Handle time zone
            if (date != null) {
                outputFormat.format(date)
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextViewWithLabel(textView: TextView, label: String, text: String?) {
        if (text.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = "$label $text"
            textView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLoginRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_required, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        val titleTextView: TextView = dialogView.findViewById(R.id.dialogTitle)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialogMessage)
        val loginButton: CardView = dialogView.findViewById(R.id.btnLoginNow)
        val cancelButton: CardView = dialogView.findViewById(R.id.btnMaybeLater)

        titleTextView.text = "Login Required"
        messageTextView.text = "You need to be logged in to access this feature."

        loginButton.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.show()
    }

    private fun setTextViewVisibility(textView: TextView, text: String?) {
        if (text.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = text
            textView.visibility = View.VISIBLE
        }
    }

    private fun setTextViewVisibilityWithStar(textView: TextView, text: String?) {
        if (text.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = text
            textView.visibility = View.VISIBLE
            val starDrawable = ContextCompat.getDrawable(this, R.drawable.ic_star)
            textView.setCompoundDrawablesWithIntrinsicBounds(starDrawable, null, null, null)
            textView.compoundDrawablePadding = 8
        }
    }

    private fun getFullImageUrl(imageUrl: String?, baseUrl: String): String? {
        return if (!imageUrl.isNullOrEmpty()) {
            if (imageUrl.startsWith("http")) imageUrl else baseUrl + imageUrl
        } else null
    }

    private fun sharePropertyDetails(property: JSONObject) {
        val baseUrl = "https://propertybhandar.com"
        val photoMainUrl = getFullImageUrl(property.optString("photo_main"), baseUrl)
        val title = property.optString("title", "")
        val address = property.optString("address", "")
        val area = property.optString("area", "")
        val price = property.optString("price", "")
        val propertyType = property.optString("property_type", "")
        val shortDescription = if (property.optString("description", "").length >= 100) {
            property.optString("description", "").substring(0, 100) // First 100 characters
        } else {
            property.optString("description", "") // Use the whole description if it's shorter
        }
        val propertyId = property.optString("id")
        val link = "https://propertybhandar.com/property/$propertyId" // Deep link or website URL

        val shareText = """
            🏡 Explore this stunning property on PropertyBhandar:
        
            🔹 *Title*: $title
            📍 *Location*: $address
            📏 *Area*: $area Sq.Ft
            💰 *Price*: ₹$price
            🏠 *Type*: $propertyType
            
            📝 *Description*: 
            $shortDescription...
        
            For complete details and more high-quality images, visit the property listing:
            🌐 $link
        
            📲 Looking for more properties? Download the PropertyBhandar app for easy access to a wide range of listings: 
            https://play.google.com/store/apps/details?id=com.ams.propertybhandar
            
            Don't miss out on this opportunity! Start your journey to finding the perfect property today.
        """.trimIndent()


        // Check if image URL is available

        // If photo URL is available, download the image and share it
        if (!photoMainUrl.isNullOrEmpty()) {
            Picasso.get().load(photoMainUrl).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        sharePropertyWithImage(shareText, bitmap)
                    } else {
                        sharePropertyWithoutImage(shareText) // Fallback if image load fails
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    sharePropertyWithoutImage(shareText) // Fallback if image load fails
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    // You can show a loading indicator if needed
                }
            })
        } else {
            // If no image available, just share text
            sharePropertyWithoutImage(shareText)
        }
    }

    private fun sharePropertyWithImage(shareText: String, bitmap: Bitmap) {
        // Save the bitmap to cache to get a Uri for sharing
        val imageUri = saveImageToCache(bitmap)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri) // Share the image
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temporary read permission

        startActivity(Intent.createChooser(shareIntent, "Share property details via"))
    }

    private fun sharePropertyWithoutImage(shareText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(shareIntent, "Share property details via"))
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri {
        val cachePath = File(applicationContext.cacheDir, "images")
        cachePath.mkdirs() // Ensure the directory exists
        val file = File(cachePath, "shared_property_image.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()

        // Use the correct authority from your AndroidManifest.xml
        return FileProvider.getUriForFile(applicationContext, "com.ams.propertybhandar.fileprovider", file)
    }

}
