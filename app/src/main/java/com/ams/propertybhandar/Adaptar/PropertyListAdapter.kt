package com.ams.propertybhandar.Adaptar

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONArray
import org.json.JSONObject

class PropertyListAdapter(
    private val context: Context,
    private var properties: JSONArray,
    private val onItemClickListener: (JSONObject) -> Unit,
    private val onWishlistToggleListener: (JSONObject, Boolean) -> Unit // Add this
) : RecyclerView.Adapter<PropertyListAdapter.PropertyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.propertylist_recycle, parent, false)
        return PropertyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties.getJSONObject(position)

        holder.titleTextView.text = property.getString("title")
        holder.priceTextView.text = "₹${property.getString("price")}"

        // Handle image loading dynamically based on whether the URL is complete or not
        val imageUrl = property.optString("photo_main", "")
        if (imageUrl.isNotEmpty()) {
            if (imageUrl.startsWith("http")) {
                // If URL already starts with "http", assume it's a complete URL
                Glide.with(context)
                    .load(imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.placeholder) // Placeholder image while loading
                            .error(R.drawable.error) // Error image if the load fails
                    )
                    .into(holder.propertyImageView)
            } else {
                // If it's a relative URL, prepend the base URL
                val baseUrl = "https://propertybhandar.com" // Adjust this if necessary
                val fullImageUrl = baseUrl + imageUrl
                Glide.with(context)
                    .load(fullImageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.placeholder) // Placeholder image while loading
                            .error(R.drawable.error) // Error image if the load fails
                    )
                    .into(holder.propertyImageView)
            }
        } else {
            holder.propertyImageView.setImageResource(R.drawable.placeholder) // Use placeholder if URL is empty
        }

        holder.addressTextView.text = property.optString("address", "N/A")
        holder.locationTextView.text = "${property.optString("city", "Unknown")}, ${property.optString("zipcode", "000000")}"

        // Set click listener for the property item
        holder.itemView.setOnClickListener {
            onItemClickListener(property) // Trigger the listener with the selected property data
        }
    }


    override fun getItemCount(): Int = properties.length()

    @SuppressLint("NotifyDataSetChanged")
    fun updateProperties(newProperties: JSONArray) {
        properties = newProperties
        notifyDataSetChanged()
    }

    class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyImageView: ShapeableImageView = itemView.findViewById(R.id.propertyImage)
        val titleTextView: TextView = itemView.findViewById(R.id.propertyTitle)
        val priceTextView: TextView = itemView.findViewById(R.id.propertyPrice)
        val addressTextView: TextView = itemView.findViewById(R.id.propertyaddress)
        val locationTextView: TextView = itemView.findViewById(R.id.propertyLocation)

    }
}
