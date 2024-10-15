package com.ams.propertybhandar.Adaptar

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
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Activity.HomeActivity
import com.ams.propertybhandar.Activity.PropertyDetailsActivity
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class RecommandedProppertyAdapter(
    private val context: Context,
    private val networkClient: NetworkClient,
    private val properties: JSONArray
) : RecyclerView.Adapter<RecommandedProppertyAdapter.ViewHolder>() {

    private var wishlist: JSONArray? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recomanded_property, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val property = properties.getJSONObject(position)
        val propertyType = property.optString("property_type", "")
        val listingId = property.optInt("id", 0) // Assuming "id" field exists

        // Set text only if the value is not empty
        holder.title.text = property.optString("title", "").ifEmpty { "N/A" }
        holder.address.text = property.optString("address", "").ifEmpty { "N/A" }

        val priceValue = property.optString("price", "")
        val areaValue = property.optString("area", "")

        // Display price based on property_type, only if values are not empty
        holder.price.text = when (propertyType.lowercase()) {
            "plot" -> if (areaValue.isNotEmpty()) "₹$areaValue Sq.ft" else "N/A"
            "flat", "shop", "house", "apartment" -> if (priceValue.isNotEmpty()) "₹$priceValue" else "N/A"
            else -> if (priceValue.isNotEmpty()) "₹$priceValue" else "N/A"
        }

        if (propertyType.lowercase() == "plot") {
            holder.bedsIcon.visibility = View.GONE
            holder.beds.visibility = View.GONE
            holder.bathsIcon.visibility = View.GONE
            holder.baths.visibility = View.GONE
            holder.saleStatus.visibility = View.VISIBLE
            holder.saleStatus.text = if (property.optBoolean("is_for_sale", false)) "For Sale" else "N/A"
        } else {
            holder.bedsIcon.visibility = View.VISIBLE
            holder.bathsIcon.visibility = View.VISIBLE

            val bedsValue = property.optString("beds", "")
            val bathsValue = property.optString("baths", "")

            if (bedsValue.isEmpty()) {
                holder.beds.visibility = View.GONE
            } else {
                holder.beds.visibility = View.VISIBLE
                holder.beds.text = bedsValue
            }

            if (bathsValue.isEmpty()) {
                holder.baths.visibility = View.GONE
            } else {
                holder.baths.visibility = View.VISIBLE
                holder.baths.text = bathsValue
            }

            holder.saleStatus.visibility = View.GONE
        }

        // Load the image using Glide
        Glide.with(context)
            .load(property.optString("photo_main", ""))
            .placeholder(R.drawable.errorbg) // Placeholder image if the URL is empty
            .error(R.drawable.errorbg) // Error image if loading fails
            .into(holder.imageView)

        // Set click listener on the itemView
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PropertyDetailsActivity::class.java)
            intent.putExtra("property_data", property.toString()) // Pass property data as string
            context.startActivity(intent)
        }

        // Wishlist Icon Handling
        holder.wishlistIcon.setOnClickListener {
            if (networkClient.getAccessToken() != null) {
                toggleWishlist(listingId, holder)
            } else {
                (context as? HomeActivity)?.showLoginRequiredDialog()
            }
        }

        // Fetch and update wishlist state for each property on page load
        fetchWishlistData {
            updateWishlistIcon(listingId, holder)
        }
    }

    private fun updateWishlistIcon(listingId: Int, holder: ViewHolder) {
        if (networkClient.getAccessToken() != null && wishlist != null) {
            val isInWishlist = wishlist?.let { checkListingInWishlist(it, listingId) } ?: false
            holder.wishlistIcon.setImageResource(if (isInWishlist) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline)
        } else {
            holder.wishlistIcon.setImageResource(R.drawable.ic_favorite_outline)
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


    private fun toggleWishlist(listingId: Int, holder: ViewHolder) {
        val isCurrentlyInWishlist = wishlist?.let { checkListingInWishlist(it, listingId) } ?: false

        networkClient.toggleWishlist(listingId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WishlistToggle", "Failed to toggle wishlist: ${e.message}")
                runOnUiThread {
                    Toast.makeText(context, "Error toggling wishlist. Please check your connection.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val successMessage = if (!isCurrentlyInWishlist) "Property added to wishlist!" else "Property removed from wishlist!"
                    runOnUiThread {
                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                        // Update the wishlist data locally (optional, depends on your needs)
                        if (isCurrentlyInWishlist) {
                            removeFromWishlist(listingId)
                        } else {
                            addToWishlist(listingId)
                        }
                        fetchWishlistData {
                            updateWishlistIcon(listingId, holder)
                        }
                    }
                } else {
                    Log.e("WishlistToggle", "API error: ${response.code} - ${response.message}")
                    runOnUiThread {
                        handleToggleWishlistError(response) //call helper function to handle error
                    }
                }
            }
        })
    }

    private fun handleToggleWishlistError(response: Response) {
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

    private fun addToWishlist(listingId: Int) {
        wishlist?.put(JSONObject().apply {
            put("listing", listingId)
        })
    }

    private fun removeFromWishlist(listingId: Int) {
        wishlist?.let {
            for (i in 0 until it.length()) {
                val wishlistItem = it.getJSONObject(i)
                if (wishlistItem.getInt("listing") == listingId) {
                    it.remove(i)
                    break
                }
            }
        }
    }


    private fun fetchWishlistData(callback: () -> Unit) {
        networkClient.fetchWishlist(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WishlistFetch", "Failed to fetch wishlist: ${e.message}")
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

    private fun runOnUiThread(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post(action)
    }

    override fun getItemCount(): Int {
        return properties.length()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val title: TextView = itemView.findViewById(R.id.title)
        val address: TextView = itemView.findViewById(R.id.textView8)
        val bedsIcon: ImageView = itemView.findViewById(R.id.imageView5)
        val beds: TextView = itemView.findViewById(R.id.textView9)
        val bathsIcon: ImageView = itemView.findViewById(R.id.imageView6)
        val baths: TextView = itemView.findViewById(R.id.textView10)
        val price: TextView = itemView.findViewById(R.id.textView11)
        val wishlistIcon: ImageView = itemView.findViewById(R.id.wishlistselector)
        val saleStatus: TextView = itemView.findViewById(R.id.saleStatus) // Assuming you add this TextView to your layout
    }
}