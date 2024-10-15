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


class LatestPropertyAdapter(
    private val context: Context,
    private val networkClient: NetworkClient, // Add NetworkClient
    private val propertyList: JSONArray
) : RecyclerView.Adapter<LatestPropertyAdapter.PropertyViewHolder>() {

    private var wishlist: JSONArray? = null
    private val mainHandler = Handler(Looper.getMainLooper())


    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView3)
        val title: TextView = itemView.findViewById(R.id.textView)
        val address: TextView = itemView.findViewById(R.id.textView4)
        val area: TextView = itemView.findViewById(R.id.textView5)
        val direction: TextView = itemView.findViewById(R.id.textView6)
        val price: TextView = itemView.findViewById(R.id.textView7)
        val wishlistIcon: ImageView = itemView.findViewById(R.id.wishlistselector) // Add wishlist icon
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.latest_property, parent, false)
        return PropertyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = propertyList.getJSONObject(position)
        val listingId = property.optInt("id", 0) // Get listing ID

        holder.title.text = property.optString("title", "No Title")
        holder.address.text = property.optString("address", "No Address")
        holder.area.text = property.optString("area", "No Area")
        holder.direction.text = property.optString("property_facing", "No Direction")

        val propertyType = property.optString("property_type", "")
        val priceValue = property.optString("price", "No Price")
        val areaValue = property.optString("area", "No Area")

        holder.price.text = when (propertyType.lowercase()) {
            "plot" -> "₹$areaValue Sq.ft"
            "flat", "shop", "house", "apartment" -> "₹$priceValue"
            else -> "₹$priceValue"
        }

        Glide.with(context)
            .load(property.optString("photo_main", ""))
            .placeholder(R.drawable.errorbg)
            .error(R.drawable.errorbg)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PropertyDetailsActivity::class.java)
            intent.putExtra("property_data", property.toString())
            context.startActivity(intent)
        }

        holder.wishlistIcon.setOnClickListener {
            if (networkClient.getAccessToken() != null) {
                toggleWishlist(listingId, holder)
            } else {
                (context as? HomeActivity)?.showLoginRequiredDialog()
            }
        }

        // Fetch and update wishlist state
        fetchWishlistData {
            updateWishlistIcon(listingId, holder)
        }
    }

    // ... (updateWishlistIcon, checkListingInWishlist, addToWishlist, removeFromWishlist, and fetchWishlistData functions remain the same as in RecommandedProppertyAdapter) ...


    private fun toggleWishlist(listingId: Int, holder: PropertyViewHolder) {
        val isCurrentlyInWishlist = wishlist?.let { checkListingInWishlist(it, listingId) } ?: false

        networkClient.toggleWishlist(listingId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WishlistToggle", "Failed to toggle wishlist: ${e.message}")
                mainHandler.post {
                    Toast.makeText(context, "Error toggling wishlist. Please check your connection.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val successMessage = if (!isCurrentlyInWishlist) "Property added to wishlist!" else "Property removed from wishlist!"
                    mainHandler.post {
                        Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                        // Update the wishlist data locally (optional)
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
                    mainHandler.post {
                        handleToggleWishlistError(response)
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


    private fun updateWishlistIcon(listingId: Int, holder: PropertyViewHolder) {
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
                callback()
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

    override fun getItemCount(): Int {
        return propertyList.length()
    }
}