package com.ams.propertybhandar.Domin

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

class NetworkClient(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "com.ams.propertybhandar.PREFS"
        private const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"
        private const val REFRESH_TOKEN_KEY = "REFRESH_TOKEN"
        private const val BASE_URL = "https://propertybhandar.com"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private val gson = Gson()
    }
    val client: OkHttpClient = OkHttpClient()
    // Login User
    fun loginUser(email: String, password: String, callback: Callback) {
        val url = "$BASE_URL/api/auth/login/"
        val jsonObject = JsonObject().apply {
            addProperty("username", email)
            addProperty("password", password)
        }
        val json = gson.toJson(jsonObject)
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkClient", "Failed to login user", e)
                callback.onFailure(call, e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("NetworkClient", "Unexpected response code: ${response.code}")
                    callback.onFailure(call, IOException("Unexpected response code: ${response.code}"))
                    return
                }

                val responseData = response.body?.string()
                val jsonResponse = gson.fromJson(responseData, JsonObject::class.java)

                val accessToken = jsonResponse.get("access")?.asString
                val refreshToken = jsonResponse.get("refresh")?.asString

                if (accessToken != null && refreshToken != null) {
                    saveTokens(accessToken, refreshToken)
                    callback.onResponse(call, response)
                } else {
                    Log.e("NetworkClient", "Access or refresh token not found in response")
                    callback.onFailure(call, IOException("Access or refresh token not found in response"))
                }
            }
        })
    }
    // Register User
    fun registerUser(email: String, password: String, contact: String, callback: Callback) {
        val url = "$BASE_URL/api/users/sign_up/"
        val json = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
            addProperty("contact", contact)
            addProperty("i_agree", "yes")
        }.toString()
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(callback)
    }
    // Verify OTP
    fun verifyOtp(email: String, otp: String, callback: Callback) {
        val url = "$BASE_URL/api/users/verify_signup_otp/"
        val json = JsonObject().apply {
            addProperty("email", email)
            addProperty("otp", otp)
        }.toString()
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(callback)
    }

    fun resendOtp(email: String, callback: Callback) {
        val request = Request.Builder()
            .url("https://www.propertybhandar.com/api/users/resend_signup_otp/")
            .post(
                FormBody.Builder()
                    .add("email", email)
                    .build()
            )
            .build()

        client.newCall(request).enqueue(callback)
    }

    // Forgot Password
    fun forgotPassword(email: String, callback: Callback) {
        val url = "$BASE_URL/api/users/password_reset_request/"
        val json = JsonObject().apply {
            addProperty("email", email)
        }.toString()
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(callback)
    }
    // Fetch Properties
    fun fetchProperties(callback: Callback) {
        val url = "$BASE_URL/api/listings/"
        makeRequestWithoutToken(url, callback)
    }

    fun submitContactForm(name: String, email: String, subject: String, message: String, phone: String, callback: Callback) {
        val url = "https://www.propertybhandar.com/api/contact/"
        val json = JsonObject().apply {
            addProperty("name", name)
            addProperty("email", email)
            addProperty("contact_number", phone)
            addProperty("subject", subject)
            addProperty("message", message)
        }.toString()
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).enqueue(callback)

    }

    fun makeRequestWithoutToken(url: String, callback: Callback) {
        val client = OkHttpClient()

        // Build the request without adding any token in the headers
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(callback)
    }

    //makeAuthenticatedRequest(url, Request.Builder().url(url).build(), callback)
    // Verify Password Reset OTP
    fun verifyPasswordResetOtp(email: String, otp: String, callback: Callback) {
        val url = "$BASE_URL/api/users/verify_password_reset_otp/"
        val json = JsonObject().apply {
            addProperty("email", email)
            addProperty("otp", otp)
        }.toString()
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(callback)
    }

    fun resendpassOtp(email: String, callback: Callback) {
        val request = Request.Builder()
            .url("https://www.propertybhandar.com/api/users/resend_password_reset_otp/")
            .post(
                FormBody.Builder()
                    .add("email", email)
                    .build()
            )
            .build()

        client.newCall(request).enqueue(callback)
    }
    // Reset Password
    fun resetPassword(newPassword: String, confirmPassword: String, otp: String, callback: Callback) {
        val url = "https://www.propertybhandar.com/api/users/password_reset_confirm/"
        val requestBody = JSONObject().apply {
            put("new_password", newPassword)
            put("confirm_password", confirmPassword)
            put("otp", otp)
            put("uid", "MTI")  // Defaulting uid to "MTI" if not provided
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)
    }
    // Fetch User Profile
    fun fetchUserProfile(callback: Callback) {
        val url = "$BASE_URL/api/profiles/profile/"
        makeAuthenticatedRequest(url, Request.Builder().url(url).build(), callback)
    }
    // Update User Profile
    fun updateUserProfile(userId: String, updatedData: JSONObject, callback: Callback) {
        val url = "$BASE_URL/api/profiles/edit_profile/" // Ensure this is correct
        val requestBody = updatedData.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Authorization", "Bearer ${getAccessToken()}")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(callback)
    }
    fun uploadUserProfileWithImage(userId: String, requestBody: RequestBody, callback: Callback) {
        val url = "$BASE_URL/api/profiles/edit_profile/"

        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .addHeader("Authorization", "Bearer ${getAccessToken()}") // Replace with your method to get the token
            .build()

        client.newCall(request).enqueue(callback)
    }

    // Submit Property Form with Photos
    fun submitPropertyWithPhotos(
        propertyData: Map<String, String>,
        photoMainFile: File?,
        photo1File: File?,
        photo2File: File?,
        photo3File: File?,
        photo4File: File?,
        callback: Callback
    ) {
        val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        propertyData.forEach { (key, value) ->
            requestBodyBuilder.addFormDataPart(key, value)
        }
        photoMainFile?.let {
            requestBodyBuilder.addFormDataPart("photo_main", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }
        photo1File?.let {
            requestBodyBuilder.addFormDataPart("photo_1", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }
        photo2File?.let {
            requestBodyBuilder.addFormDataPart("photo_2", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }
        photo3File?.let {
            requestBodyBuilder.addFormDataPart("photo_3", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }
        photo4File?.let {
            requestBodyBuilder.addFormDataPart("photo_4", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull()))
        }
        val requestBody = requestBodyBuilder.build()
        val request = Request.Builder()
            .url("$BASE_URL/api/listings/")
            .post(requestBody)
            .addHeader("Authorization", "Bearer ${getAccessToken()}")
            .build()

        client.newCall(request).enqueue(callback)
    }
    fun refreshToken(refreshToken: String, callback: Callback) {
        val requestBody = FormBody.Builder()
            .add("refresh_token", refreshToken)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/api/auth/refresh/")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)
    }
    // Handle token-based requests
    internal fun makeAuthenticatedRequest(url: String, request: Request, callback: Callback) {
        val accessToken = getAccessToken() ?: return

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(authenticatedRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkClient", "Network request failed", e)
                callback.onFailure(call, e)
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> callback.onResponse(call, response) // Successful response
                    401 -> handleTokenRefresh { success ->
                        if (success) {
                            // Retry the original request after token refresh
                            makeAuthenticatedRequest(url, request, callback)
                        } else {
                            callback.onFailure(call, IOException("Token refresh failed"))
                        }
                    }
                    else -> {
                        callback.onFailure(call, IOException("Unexpected response code: ${response.code}"))
                    }
                }
            }
        })
    }

    // Search Properties
    fun searchProperties(keywords: String, callback: Callback) {
        // Construct the URL with the base URL and the "keywords" query parameter
        val url = "https://propertybhandar.com/api/search/".toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("keywords", keywords)
            ?.build()

        if (url != null) {
            Log.d("PropertySearch", "Request URL: $url")  // Log the constructed URL

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).enqueue(callback)
        } else {
            Log.e("PropertySearch", "Failed to build URL")
        }
    }

    fun toggleWishlist(listingId: Int, callback: Callback) {
        val url = "$BASE_URL/api/wishlist/$listingId/toggle/"
        val request = Request.Builder()
            .url(url)
            .post("".toRequestBody(JSON))
            .addHeader("Authorization", "Bearer ${getAccessToken()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkClient", "Wishlist toggle failed (onFailure): ${e.message}", e)
                callback.onFailure(call, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("NetworkClient", "Wishlist toggled successfully. Response code: ${response.code}")
                    try {
                        val responseBody = response.body?.string()
                        Log.d("NetworkClient", "Response body: $responseBody")
                        callback.onResponse(call, response)
                    } catch (e: Exception) {
                        Log.e("NetworkClient", "Error parsing response body: ${e.message}", e)
                        callback.onFailure(call, IOException("Error parsing response body"))
                    }
                } else {
                    Log.e("NetworkClient", "Wishlist toggle failed. Response code: ${response.code} - ${response.message}")
                    try {
                        val responseBody = response.body?.string() // Get the error response
                        Log.e("NetworkClient", "Error response body: $responseBody") //Log the response body to check for server side errors
                    } catch (e: Exception) {
                        Log.e("NetworkClient", "Error parsing error response body", e)
                    }
                    when (response.code) {
                        401 -> { //Handle 401 Unauthorized specifically
                            handleTokenRefresh { success ->
                                if (success) {
                                    // Retry the request
                                    toggleWishlist(listingId, callback)
                                } else {
                                    callback.onFailure(call, IOException("Token refresh failed"))
                                }
                            }
                        }
                        else -> {
                            callback.onFailure(call, IOException("Wishlist toggle failed. Server responded with code ${response.code}"))
                        }
                    }
                }
                response.close()
            }
        })
    }

    fun checkWishlist(listingId: Int, callback: Callback) {
        val url = "$BASE_URL/api/wishlist/$listingId/"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Authorization", "Bearer ${getAccessToken()}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkClient", "Failed to check wishlist: ${e.message}", e)
                callback.onFailure(call, e) // Pass the exception up
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()
                        val jsonObject = responseBody?.let { JSONObject(it) }
                        val isInWishlist = jsonObject?.getBoolean("in_wishlist")
                        if (responseBody != null) {
                            callback.onResponse(call, Response.Builder().code(200).body(responseBody.toResponseBody(JSON)).message("Wishlist check successful").request(request).build())
                        } //Pass modified response for further processing

                    } catch (e: Exception) {
                        Log.e("NetworkClient", "Error parsing wishlist check response: ${e.message}", e)
                        callback.onFailure(call, IOException("Error parsing wishlist check response"))
                    }
                } else if (response.code == 401) {
                    //Handle Unauthorized, potentially trigger token refresh or redirect to login
                    callback.onFailure(call, IOException("Unauthorized. Please log in."))

                } else {
                    callback.onFailure(call, IOException("Wishlist check failed: ${response.code}"))
                }
                response.close()
            }
        })
    }

    // Fetch Wishlist
    fun fetchWishlist(callback: Callback) {
        val url = "$BASE_URL/api/wishlist/"
        makeAuthenticatedRequest(url, Request.Builder().url(url).get().build(), callback)
    }


    // Fetch Property Details
    fun fetchPropertyDetails(listingId: Int, callback: Callback) {
        val url = "$BASE_URL/api/listings/$listingId/"
        makeAuthenticatedRequest(url, Request.Builder().url(url).get().build(), callback)
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            apply()
        }
    }
    // Token management
    private fun saveTokens(accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(ACCESS_TOKEN_KEY, accessToken)
            putString(REFRESH_TOKEN_KEY, refreshToken)
            apply()
        }
    }
    internal fun getAccessToken(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }
    internal fun getRefreshToken(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }
    private fun handleTokenRefresh(callback: (Boolean) -> Unit) {
        val refreshToken = getRefreshToken() ?: run {
            callback(false)
            return
        }
        val url = "$BASE_URL/api/auth/refresh/"
        val jsonObject = JsonObject().apply {
            addProperty("refresh", refreshToken)
        }
        val json = gson.toJson(jsonObject)
        val body = json.toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkClient", "Token refresh failed", e)
                callback(false)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(false)
                    return
                }
                val responseData = response.body?.string()
                val jsonResponse = gson.fromJson(responseData, JsonObject::class.java)
                val newAccessToken = jsonResponse.get("access")?.asString
                val newRefreshToken = jsonResponse.get("refresh")?.asString
                if (newAccessToken != null && newRefreshToken != null) {
                    saveTokens(newAccessToken, newRefreshToken) // Save the new tokens
                    callback(true)
                } else {
                    callback(false)
                }
            }
        })
    }
}