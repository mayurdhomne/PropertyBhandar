package com.ams.propertybhandar.Adaptar

import android.annotation.SuppressLint
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

class LatestPropertyAdapter(
    private val context: Context,
    private val propertyList: JSONArray
) : RecyclerView.Adapter<LatestPropertyAdapter.PropertyViewHolder>() {

    inner class PropertyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView3)
        val title: TextView = itemView.findViewById(R.id.textView)
        val address: TextView = itemView.findViewById(R.id.textView4)
        val area: TextView = itemView.findViewById(R.id.textView5)
        val direction: TextView = itemView.findViewById(R.id.textView6)
        val price: TextView = itemView.findViewById(R.id.textView7)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.latest_property, parent, false)
        return PropertyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = propertyList.getJSONObject(position)
        holder.title.text = property.optString("title", "No Title")
        holder.address.text = property.optString("address", "No Address")
        holder.area.text = property.optString("area", "No Area")
        holder.direction.text = property.optString("property_facing", "No Direction")
        holder.price.text = "₹${property.optString("price", "No Price")}"

        // Load image using Glide
        Glide.with(context)
            .load(property.optString("photo_main", ""))
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PropertyDetailsActivity::class.java)
            intent.putExtra("property_data", property.toString()) // Pass property data as string
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return propertyList.length()
    }
}
