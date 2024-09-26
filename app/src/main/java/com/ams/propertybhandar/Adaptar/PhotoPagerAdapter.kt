package com.ams.propertybhandar.Adaptar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ams.propertybhandar.R
import com.bumptech.glide.Glide

class PhotoPagerAdapter(
    private val context: Context,
    private val photoUrls: List<String>,
    private val viewPager: ViewPager2
) : RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_property_image, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        Glide.with(context)
            .load(photoUrls[position])
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = photoUrls.size

    // Adding a custom page transformer for advanced animations
    init {
        viewPager.setPageTransformer(ZoomOutPageTransformer())  // or another custom transformer
    }
    // Example of an advanced PageTransformer for zoom-out effect
    class ZoomOutPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.apply {
                val scaleFactor = Math.max(0.85f, 1 - Math.abs(position))
                val verticalMargin = height * (1 - scaleFactor) / 2
                val horizontalMargin = width * (1 - scaleFactor) / 2
                translationX = if (position < 0) horizontalMargin - verticalMargin / 2
                else horizontalMargin + verticalMargin / 2

                scaleX = scaleFactor
                scaleY = scaleFactor

                alpha = (0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * 0.5f).coerceAtLeast(0.3f)
            }
        }
    }
}


