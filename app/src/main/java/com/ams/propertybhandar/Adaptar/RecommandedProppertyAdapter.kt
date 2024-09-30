package com.ams.propertybhandar.Adaptar

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.Activity.PropertyDetailsActivity
import com.ams.propertybhandar.R
import com.bumptech.glide.Glide
import org.json.JSONArray

class RecommandedProppertyAdapter(
    private val context: Context,
    private val properties: JSONArray
) : RecyclerView.Adapter<RecommandedProppertyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recomanded_property, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val property = properties.getJSONObject(position)
        val propertyType = property.optString("property_type", "")

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
        val saleStatus: TextView = itemView.findViewById(R.id.saleStatus) // Assuming you add this TextView to your layout
    }
}