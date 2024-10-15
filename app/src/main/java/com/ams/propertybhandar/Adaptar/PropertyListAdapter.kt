package com.ams.propertybhandar.Adaptar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class PropertyListAdapter(
    private val context: Context,
    private var properties: JSONArray,
    private val onItemClickListener: (JSONObject) -> Unit,
    private val onWishlistToggleListener: (JSONObject, Boolean) -> Unit,
    private val networkClient: NetworkClient
) : RecyclerView.Adapter<PropertyListAdapter.PropertyViewHolder>() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var wishlist: JSONArray? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.propertylist_recycle, parent, false)
        return PropertyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        try {
            val property = properties.getJSONObject(position)
            val listingId = property.getInt("id")

            // Log the initial state of isInWishlist
            Log.d("PropertyListAdapter", "Property ID: $listingId, isInWishlist: ${property.optBoolean("isInWishlist", false)}")

            holder.titleTextView.text = property.optString("title", "Unknown Property")
            val propertyType = property.optString("property_type", "")
            val priceValue = property.optString("price", "No Price")
            val areaValue = property.optString("area", "No Area")

            holder.priceTextView.text = when (propertyType.lowercase()) {
                "plot" -> "₹$areaValue Sq.ft"
                "flat", "shop", "house", "apartment" -> "₹$priceValue"
                else -> "₹$priceValue"
            }

            val imageUrl = property.optString("photo_main", "")
            if (imageUrl.isNotEmpty()) {
                val baseUrl = "https://propertybhandar.com"
                val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl else baseUrl + imageUrl
                Glide.with(context)
                    .load(fullImageUrl)
                    .apply(RequestOptions().placeholder(R.drawable.errorbg).error(R.drawable.errorbg))
                    .into(holder.propertyImageView)
            } else {
                holder.propertyImageView.setImageResource(R.drawable.errorbg)
            }

            holder.addressTextView.text = property.optString("address", "N/A")
            holder.locationTextView.text = "${property.optString("city", "Unknown")}, ${property.optString("zipcode", "000000")}"

            holder.itemView.setOnClickListener {
                onItemClickListener(property)
            }
// Set initial wishlist icon state based on whether the property is in the wishlist
            updateWishlistIcon(holder.wishlistIcon, property.optBoolean("isInWishlist", false))

            holder.wishlistIcon.setOnClickListener {
                if (networkClient.getAccessToken() != null) {
                    toggleWishlist(property, holder.wishlistIcon)
                } else {
                    // Handle unauthorized access (e.g., show login dialog)
                    Toast.makeText(context, "Please log in to manage your wishlist.", Toast.LENGTH_SHORT).show()
                }
            }

            // Fetch and update wishlist state for each property
            fetchWishlistData {
                updateWishlistIcon(holder.wishlistIcon, listingId)
            }
        } catch (e: Exception) {
            Log.e("PropertyListAdapter", "Error binding data: ${e.message}", e)
            handleError("Error loading property data")
        }

    }

    override fun getItemCount(): Int = properties.length()

    @SuppressLint("NotifyDataSetChanged")
    fun updateProperties(newProperties: JSONArray) {
        properties = newProperties
        notifyDataSetChanged()
    }

    private fun toggleWishlist(property: JSONObject, wishlistIcon: ImageView) {
        val listingId = property.getInt("id")
        val isInWishlist = property.optBoolean("isInWishlist", false)

        // Log the updated state of isInWishlist
        Log.d("PropertyListAdapter", "Property ID: $listingId, isInWishlist: $isInWishlist")

        // Optimistic update: Change the icon immediately
        property.put("isInWishlist", !isInWishlist)
        updateWishlistIcon(wishlistIcon, !isInWishlist)

        // Send request to toggle wishlist
        networkClient.toggleWishlist(listingId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    handleError("Network error toggling wishlist. Please check your connection.")
                    // Revert icon on failure
                    property.put("isInWishlist", isInWishlist)
                    updateWishlistIcon(wishlistIcon, isInWishlist)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    mainHandler.post {
                        val successMessage = if (!isInWishlist) "Property added to wishlist!" else "Property removed from wishlist!"
                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                        onWishlistToggleListener(property, !isInWishlist)
                        notifyItemChanged(findIndexOfProperty(property))
                    }
                } else {
                    mainHandler.post {
                        handleToggleWishlistError(response)
                        // Revert icon on failure
                        property.put("isInWishlist", isInWishlist)
                        updateWishlistIcon(wishlistIcon, isInWishlist)
                    }
                }
            }
        })
    }

    // Improved error handling
    private fun handleToggleWishlistError(response: Response) {
        mainHandler.post {
            try {
                val errorBody = response.body?.string()
                val errorMessage = errorBody ?: "Unknown error toggling wishlist."
                val errorJson = try {
                    JSONObject(errorBody)
                } catch (e: Exception) {
                    null
                }

                val detailedMessage = when {
                    errorJson != null && errorJson.has("detail") -> errorJson.getString("detail")
                    errorJson != null && errorJson.has("message") -> errorJson.getString("message")
                    else -> errorMessage
                }

                Toast.makeText(context, detailedMessage, Toast.LENGTH_LONG).show()
                Log.e("WishlistAdapter", "Error toggling wishlist: $detailedMessage, Response Code: ${response.code}", null)
            } catch (e: Exception) {
                Toast.makeText(context, "Error processing server response.", Toast.LENGTH_LONG).show()
                Log.e("WishlistAdapter", "Error processing server response: ${e.message}", e)
            }
        }
    }

    private fun updateWishlistIcon(wishlistIcon: ImageView, isInWishlist: Boolean) {
        val iconResId = if (isInWishlist) {
            R.drawable.ic_favorite_filled // Filled heart icon
        } else {
            R.drawable.ic_favorite_outline // Outline heart icon
        }
        wishlistIcon.setImageResource(iconResId)
    }


    private fun findIndexOfProperty(property: JSONObject): Int {
        for (i in 0 until properties.length()) {
            if (properties.getJSONObject(i).getInt("id") == property.getInt("id")) {
                return i
            }
        }
        return -1
    }

    private fun updateWishlistIcon(wishlistIcon: ImageView, listingId: Int) {
        if (networkClient.getAccessToken() != null && wishlist != null) {
            val isInWishlist = wishlist?.let { checkListingInWishlist(it, listingId) } ?: false
            wishlistIcon.setImageResource(if (isInWishlist) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline)
        } else {
            wishlistIcon.setImageResource(R.drawable.ic_favorite_outline)
        }
    }

    private fun checkListingInWishlist(wishlist: JSONArray, listingId: Int): Boolean {
        for (i in 0 until wishlist.length()) {
            val wishlistItem = wishlist.getJSONObject(i)
            if (wishlistItem.getInt("listing") == listingId) {
                return true
            }
        }
        return false
    }

    private fun fetchWishlistData(callback: () -> Unit) {
        networkClient.fetchWishlist(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WishlistFetch", "Failed to fetch wishlist: ${e.message}")
                callback() // Call callback even on failure to avoid blocking
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        wishlist = JSONArray(response.body?.string())
                    } catch (e: Exception) {
                        Log.e("WishlistFetch", "Error parsing JSON: ${e.message}")
                    }
                } else {
                    Log.e("WishlistFetch", "API error: ${response.code} - ${response.message}")
                }
                callback()
            }
        })
    }

    private fun handleError(message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyImageView: ShapeableImageView = itemView.findViewById(R.id.propertyImage)
        val titleTextView: TextView = itemView.findViewById(R.id.propertyTitle)
        val priceTextView: TextView = itemView.findViewById(R.id.propertyPrice)
        val addressTextView: TextView = itemView.findViewById(R.id.propertyaddress)
        val locationTextView: TextView = itemView.findViewById(R.id.propertyLocation)
        val wishlistIcon: ImageView = itemView.findViewById(R.id.wishlistIcon)
    }
}
