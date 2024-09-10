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
import org.json.JSONObject

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

        holder.title.text = property.optString("title", "N/A")
        holder.address.text = property.optString("address", "N/A")
        holder.beds.text = property.optString("beds", "N/A")
        holder.baths.text = property.optString("baths", "N/A")
        holder.price.text = property.optString("price", "N/A")

        // Load the image using Glide
        Glide.with(context)
            .load(property.optString("photo_main", ""))
            .placeholder(R.drawable.person) // Placeholder image if the URL is empty
            .error(R.drawable.error) // Error image if loading fails
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
        val beds: TextView = itemView.findViewById(R.id.textView9)
        val baths: TextView = itemView.findViewById(R.id.textView10)
        val price: TextView = itemView.findViewById(R.id.textView11)
    }
}

