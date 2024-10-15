package com.ams.propertybhandar.Adaptar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Activity.PropertyDetailsActivity
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

class WishlistAdapter(
    private val context: Context,
    private var properties: JSONArray,
    private val onPropertyClick: (JSONObject) -> Unit,
    private val networkClient: NetworkClient
) : RecyclerView.Adapter<WishlistAdapter.PropertyViewHolder>() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val wishlistIds = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.propertylist_recycle, parent, false)
        return PropertyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        try {
            val property = properties.getJSONObject(position)
            val listingId = property.getInt("id")

            holder.titleTextView.text = property.optString("title", "Unknown Property")
            holder.priceTextView.text = "₹${property.optString("price", "0")}"
            holder.addressTextView.text = property.optString("address", "N/A")
            holder.locationTextView.text = "${property.optString("city", "Unknown")}, ${property.optString("zipcode", "000000")}"

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

            holder.itemView.setOnClickListener {
                val intent = Intent(context, PropertyDetailsActivity::class.java).apply {
                    putExtra("propertyData", property.toString())
                }
                context.startActivity(intent)
            }

            // Wishlist Icon Click Listener
            holder.wishlistIcon.setOnClickListener {
                toggleWishlist(listingId, holder.wishlistIcon, property)
            }

            holder.itemView.setOnClickListener {
                onPropertyClick(property) // call the callback function
            }
            updateWishlistIcon(holder, property)

            // Set wishlist icon correctly
            val filledHeart = ContextCompat.getDrawable(context, R.drawable.ic_favorite_filled)

            holder.wishlistIcon.setImageDrawable(if (wishlistIds.contains(listingId)) filledHeart else filledHeart)

        } catch (e: Exception) {
            Log.e("WishlistAdapter", "Error binding data: ${e.message}")
        }
    }

    override fun getItemCount(): Int = properties.length()

    @SuppressLint("NotifyDataSetChanged")
    fun updateProperties(newProperties: JSONArray) {
        properties = newProperties
        mainHandler.post { notifyDataSetChanged() }
    }

    private fun toggleWishlist(listingId: Int, wishlistIcon: ImageView, property: JSONObject) {
        val wasInWishlist = isInWishlist(listingId)

        networkClient.toggleWishlist(listingId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    Toast.makeText(context, "Network Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("WishlistAdapter", "Network error toggling wishlist: ${e.message}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                mainHandler.post {
                    if (response.isSuccessful) {
                        refreshWishlistData()
                        val message = if (wasInWishlist) "Removed from wishlist" else "Added to wishlist"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    } else {
                        handleToggleWishlistError(response)
                    }
                }
                response.close()
            }
        })
    }

    private fun handleToggleWishlistError(response: Response) {
        try {
            val errorBody = response.body?.string()
            val errorMessage = errorBody ?: "Unknown error"
            val errorJson = try {
                errorBody?.let { JSONObject(it) }
            } catch (e: Exception) {
                null
            }

            val detailedMessage = if (errorJson != null) {
                "Error: ${errorJson.optString("message", errorMessage)}"
            } else {
                "Error: $errorMessage"
            }

            Toast.makeText(context, detailedMessage, Toast.LENGTH_LONG).show()
            Log.e("WishlistAdapter", "Error toggling wishlist: $detailedMessage, Response Code: ${response.code}", null)
        } catch (e: Exception) {
            Toast.makeText(context, "Error processing server response: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("WishlistAdapter", "Error processing server response: ${e.message}", e)
        }
    }

    private fun refreshWishlistData() {
        networkClient.fetchWishlist(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    Toast.makeText(context, "Network Error refreshing wishlist", Toast.LENGTH_SHORT).show()
                    Log.e("WishlistAdapter", "Network error refreshing wishlist: ${e.message}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("WishlistAdapter", "Wishlist fetch response code: ${response.code}")
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()
                        val wishlistItems = JSONArray(responseBody)
                        Log.d("WishlistAdapter", "Wishlist fetch response body: $wishlistItems")
                        fetchPropertyDetailsForWishlist(wishlistItems)

                    } catch (e: Exception) {
                        handleRefreshWishlistError(e, response)
                    }
                } else {
                    handleRefreshWishlistError(null, response)
                }
            }
        })
    }

    private fun fetchPropertyDetailsForWishlist(wishlistItems: JSONArray) {
        if (wishlistItems.length() == 0) {
            mainHandler.post {
                updateProperties(JSONArray())  // If empty, clear the properties
            }
            return
        }

        val listingIds = mutableListOf<Int>()
        for (i in 0 until wishlistItems.length()) {
            listingIds.add(wishlistItems.getJSONObject(i).getInt("listing"))
        }

        var count = 0
        val newWishlistProperties = JSONArray()

        for (listingId in listingIds) {
            networkClient.fetchPropertyDetails(listingId, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("WishlistAdapter", "Failed to fetch property details: ${e.message}")
                    count++
                    if (count == listingIds.size) updateProperties(newWishlistProperties)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            response.body?.use { responseBody ->
                                val propertyDetails = JSONObject(responseBody.string())
                                newWishlistProperties.put(propertyDetails)
                            }
                        } else {
                            Log.e("WishlistAdapter", "Error response code: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e("WishlistAdapter", "Error parsing JSON: ${e.message}")
                    } finally {
                        response.close()
                        count++
                        if (count == listingIds.size) updateProperties(newWishlistProperties)
                    }
                }
            })
        }
    }

    private fun handleRefreshWishlistError(e: Exception?, response: Response) {
        val errorMessage = if (e != null) {
            "Error parsing wishlist data: ${e.message}"
        } else {
            "Error refreshing wishlist, Response Code: ${response.code}"
        }

        mainHandler.post {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
        Log.e("WishlistAdapter", errorMessage, e)
    }

    private fun updateWishlistIcon(holder: PropertyViewHolder, property: JSONObject) {
        val isInWishlist = isInWishlist(property.getInt("id"))
        holder.wishlistIcon.isSelected = isInWishlist
    }

    private fun isInWishlist(listingId: Int): Boolean {
        for (i in 0 until properties.length()) {
            if (properties.getJSONObject(i).getInt("id") == listingId) {
                return true
            }
        }
        return false
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
