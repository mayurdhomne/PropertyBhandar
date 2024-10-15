package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ams.propertybhandar.Adaptar.WishlistAdapter
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AllWishlistActivity : AppCompatActivity() {

    private lateinit var propertyRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var wishlistAdapter: WishlistAdapter
    private var wishlistProperties: JSONArray = JSONArray()
    private var customLoadingDialog: CustomLoadingDialog? = null
    private lateinit var networkClient: NetworkClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_all_wishlist)

        networkClient = NetworkClient(this)
        propertyRecyclerView = findViewById(R.id.propertyRecyclerView)
        propertyRecyclerView.layoutManager = LinearLayoutManager(this)
        wishlistAdapter = WishlistAdapter(this, wishlistProperties, ::openPropertyDetails, networkClient)
        propertyRecyclerView.adapter = wishlistAdapter
        
        findViewById<ImageView>(R.id.wishadd).setOnClickListener {
            val intent = Intent(this, PropertyListActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.backic).setOnClickListener {
            onBackPressed()
        }

        fetchWishlistProperties()
    }
    private fun openPropertyDetails(property: JSONObject) {
        val intent = Intent(this, PropertyDetailsActivity::class.java).apply {
            putExtra("property_data", property.toString()) //Corrected key name
        }
        startActivity(intent)
    }
    private fun fetchWishlistProperties() {
         showLoadingDialog()
        networkClient.fetchWishlist(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WishlistFetch", "Failed to fetch wishlist: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                hideLoadingDialog()
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()
                        val wishlistItems = JSONArray(responseBody)

                        //Directly use wishlistItems to fetch property details.
                        fetchPropertyDetailsForWishlist(wishlistItems)

                    } catch (e: Exception) {
                        Log.e("WishlistFetch", "Error parsing JSON: ${e.message}")
                    }
                } else {
                    Log.e("WishlistFetch", "API error: ${response.code} - ${response.message}")
                }
            }
        })
    }

    private fun fetchPropertyDetailsForWishlist(wishlistItems: JSONArray) {

        val emptyWishlistMessage = findViewById<TextView>(R.id.emptyWishlistMessage)

        if (wishlistItems.length() == 0) {
            emptyWishlistMessage.visibility = View.VISIBLE
            wishlistAdapter.updateProperties(wishlistProperties)
            return
        } else {
            emptyWishlistMessage.visibility = View.GONE
        }

        val listingIds = mutableListOf<Int>()
        for (i in 0 until wishlistItems.length()) {
            listingIds.add(wishlistItems.getJSONObject(i).getInt("listing"))
        }

        var count = 0
        showLoadingDialog()
        for (listingId in listingIds) {
            networkClient.fetchPropertyDetails(listingId, object : Callback {
                @SuppressLint("SuspiciousIndentation")
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("WishlistFetch", "Failed to fetch property details: ${e.message}")
                    count++
                    if (count == listingIds.size)
                        hideLoadingDialog()
                        updateAdapter()
                }

                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            response.body?.use { responseBody ->
                                val propertyDetails = JSONObject(responseBody.string())
                                wishlistProperties.put(propertyDetails)
                            }
                        } else {
                            Log.e("WishlistFetch", "Error response code: ${response.code}")
                        }
                    } catch (e: Exception) {
                        Log.e("WishlistFetch", "Error parsing JSON: ${e.message}")
                    } finally {
                        response.close()
                        count++
                        if (count == listingIds.size)
                            hideLoadingDialog()
                            updateAdapter()
                    }
                }
            })
        }
    }

    private fun updateAdapter() {
        runOnUiThread {
            wishlistAdapter.updateProperties(wishlistProperties)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showLoadingDialog() {
        if (customLoadingDialog == null) {
            customLoadingDialog = CustomLoadingDialog(this)
        }
        customLoadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        customLoadingDialog?.dismiss()
    }
}