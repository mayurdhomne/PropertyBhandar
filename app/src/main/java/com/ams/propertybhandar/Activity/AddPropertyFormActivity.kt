package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern

class AddPropertyFormActivity : AppCompatActivity() {

    private lateinit var networkClient: NetworkClient
    private var photoMainUri: Uri? = null
    private var photo1Uri: Uri? = null
    private var photo2Uri: Uri? = null
    private var photo3Uri: Uri? = null
    private var photo4Uri: Uri? = null

    private lateinit var photoMainButton: Button
    private lateinit var photo1Button: Button
    private lateinit var photo2Button: Button
    private lateinit var photo3Button: Button
    private lateinit var photo4Button: Button

    companion object {
        private const val PHOTO_MAIN_REQUEST = 2
        private const val PHOTO_1_REQUEST = 3
        private const val PHOTO_2_REQUEST = 4
        private const val PHOTO_3_REQUEST = 5
        private const val PHOTO_4_REQUEST = 6
    }

    private lateinit var filenameTextView: TextView
    private lateinit var filenameTextView1: TextView
    private lateinit var filenameTextView2: TextView
    private lateinit var filenameTextView3: TextView
    private lateinit var filenameTextView4: TextView

    private val photoUploadedText = "Photo Uploaded"
    private var customLoadingDialog: CustomLoadingDialog? = null
    private var progressDialog: ProgressDialog? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_property_form)

        networkClient = NetworkClient(this)

        val titleEditText: EditText = findViewById<TextInputLayout>(R.id.titleLayout).editText!!
        val addressEditText: EditText = findViewById<TextInputLayout>(R.id.addressLayout).editText!!
        val cityEditText: EditText = findViewById<TextInputLayout>(R.id.cityLayout).editText!!
        val stateSpinner: Spinner = findViewById(R.id.state)
        val zipcodeEditText: EditText = findViewById<TextInputLayout>(R.id.zipcodeLayout).editText!!
        val descriptionEditText: EditText = findViewById<TextInputLayout>(R.id.descriptionLayout).editText!!
        val priceEditText: EditText = findViewById<TextInputLayout>(R.id.priceLayout).editText!!
        val areaEditText: EditText = findViewById<TextInputLayout>(R.id.areaLayout).editText!!
        val bedsEditText: EditText = findViewById<TextInputLayout>(R.id.bedsLayout).editText!!
        val bathsEditText: EditText = findViewById<TextInputLayout>(R.id.bathsLayout).editText!!
        val amenities1EditText: EditText = findViewById<TextInputLayout>(R.id.tvamenity1).editText!!
        val amenities2EditText: EditText = findViewById<TextInputLayout>(R.id.tvamenity2).editText!!
        val amenities3EditText: EditText = findViewById<TextInputLayout>(R.id.tvamenity3).editText!!
        val amenities4EditText: EditText = findViewById<TextInputLayout>(R.id.tvamenity4).editText!!
        val amenities5EditText: EditText = findViewById<TextInputLayout>(R.id.tvamenity5).editText!!
        val amenities6EditText: EditText = findViewById<TextInputLayout>(R.id.tvamenity6).editText!!
        val submitButton: Button = findViewById(R.id.submit)
        val backButton: ImageView = findViewById(R.id.backic)


        photoMainButton = findViewById(R.id.uploadPhotoButton)
        photo1Button = findViewById(R.id.uploadPhotoButton2)
        photo2Button = findViewById(R.id.uploadPhotoButton3)
        photo3Button = findViewById(R.id.uploadPhotoButton4)
        photo4Button = findViewById(R.id.uploadPhotoButton5)


        photoMainButton.setOnClickListener { openImagePicker(PHOTO_MAIN_REQUEST) }
        photo1Button.setOnClickListener { openImagePicker(PHOTO_1_REQUEST) }
        photo2Button.setOnClickListener { openImagePicker(PHOTO_2_REQUEST) }
        photo3Button.setOnClickListener { openImagePicker(PHOTO_3_REQUEST) }
        photo4Button.setOnClickListener { openImagePicker(PHOTO_4_REQUEST) }

        filenameTextView = findViewById(R.id.filename)
        filenameTextView1 = findViewById(R.id.filename1)
        filenameTextView2 = findViewById(R.id.filename2)
        filenameTextView3 = findViewById(R.id.filename3)
        filenameTextView4 = findViewById(R.id.filename4)


        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }


        submitButton.setOnClickListener {
            // Collect input values
            val propertyTypeSpinner: Spinner = findViewById(R.id.propertyTypeSpinner)
            val selectedPropertyType = propertyTypeSpinner.selectedItem.toString()
            val propertyFacingSpinner: Spinner = findViewById(R.id.propertyFacingSpinner)
            val selectedPropertyFacing = propertyFacingSpinner.selectedItem.toString()
            val title = titleEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val city = cityEditText.text.toString().trim()
            val selectedState = stateSpinner.selectedItem.toString()
            val zipcode = zipcodeEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            val area = areaEditText.text.toString().trim()
            val beds = bedsEditText.text.toString().trim()
            val baths = bathsEditText.text.toString().trim()
            val amenities1 = amenities1EditText.text.toString().trim()
            val amenities2 = amenities2EditText.text.toString().trim()
            val amenities3 = amenities3EditText.text.toString().trim()
            val amenities4 = amenities4EditText.text.toString().trim()
            val amenities5 = amenities5EditText.text.toString().trim()
            val amenities6 = amenities6EditText.text.toString().trim()
            val isForSale = findViewById<SwitchMaterial>(R.id.saleSwitch).isChecked
            val isForRent = findViewById<SwitchMaterial>(R.id.rentSwitch).isChecked

            if (!isForSale && !isForRent) {
                Toast.makeText(this, "Please select either For Sale or For Rent", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Validate inputs
            if (validateFields(title, address, city, selectedState, zipcode, description, price, area, amenities1, amenities2, amenities3) && photoMainUri != null) {
                val propertyData = mapOf(
                    "property_type" to selectedPropertyType,
                    "property_facing" to selectedPropertyFacing,
                    "title" to title,
                    "address" to address,
                    "city" to city,
                    "state" to selectedState,
                    "zipcode" to zipcode,
                    "description" to description,
                    "price" to price,
                    "area" to area,
                    "beds" to beds,
                    "baths" to baths,
                    "amenities1" to amenities1,
                    "amenities2" to amenities2,
                    "amenities3" to amenities3,
                    "amenities4" to amenities4,
                    "amenities5" to amenities5,
                    "amenities6" to amenities6,
                    "is_for_sale" to isForSale.toString(),
                    "is_for_rent" to isForRent.toString(),
                    "list_date" to "2024-08-23T14:15:22Z",
                )

                // Show loading dialog
                showLoadingDialog()

                // Use Kotlin coroutines to handle background tasks
                lifecycleScope.launch {
                    try {
                        val photoMainFile = photoMainUri?.let { compressAndResizeImage(it) }
                        val photo1File = photo1Uri?.let { compressAndResizeImage(it) }
                        val photo2File = photo2Uri?.let { compressAndResizeImage(it) }
                        val photo3File = photo3Uri?.let { compressAndResizeImage(it) }
                        val photo4File = photo4Uri?.let { compressAndResizeImage(it) }


                        networkClient.submitPropertyWithPhotos(
                            propertyData,
                            photoMainFile,
                            photo1File,
                            photo2File,
                            photo3File,
                            photo4File,
                            object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    runOnUiThread {
                                        Log.e("AddPropertyActivity", "Request failed: ${e.message}")
                                        Toast.makeText(this@AddPropertyFormActivity, "Failed to submit property: ${e.message}", Toast.LENGTH_LONG).show()
                                        hideLoadingDialog()
                                    }
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    lifecycleScope.launch(Dispatchers.IO) { // Use Dispatchers.IO for network operations
                                        if (response.isSuccessful) {
                                            val responseBody = response.body?.string() ?: "" // Read the response body on a background thread
                                            // ... process the responseBody ...
                                            runOnUiThread {
                                                // Update the UI with the processed data
                                                Toast.makeText(this@AddPropertyFormActivity, "Property submitted successfully!", Toast.LENGTH_LONG).show()

                                                // Navigate to HomeActivity after successful property submission
                                                val intent = Intent(this@AddPropertyFormActivity, HomeActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)

                                                // Close the current activity
                                                finish()
                                            }
                                        } else {
                                            val errorBody = response.body?.string() ?: "" // Read the error body on a background thread
                                            Log.e("AddPropertyActivity", "Error response: ${response.code} - $errorBody")
                                            runOnUiThread {
                                                Toast.makeText(this@AddPropertyFormActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        runOnUiThread {
                                            hideLoadingDialog()
                                        }
                                    }
                                }

                            }
                        )
                        hideLoadingDialog()
                    } catch (e: Exception) {
                        runOnUiThread {
                            Log.e("AddPropertyActivity", "Error: ${e.message}")
                            Toast.makeText(this@AddPropertyFormActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                            hideLoadingDialog()
                        }
                    }
                }
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Navigate to HomeActivity when back button is pressed
        val intent = Intent(this@AddPropertyFormActivity, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Close current activity
    }

    private fun validateFields(
        title: String,
        address: String,
        city: String,
        state: String,
        zipcode: String,
        description: String,
        price: String,
        area: String,
        amenities1: String,
        amenities2: String,
        amenities3: String
    ): Boolean {
        val isValid = StringBuilder()
        if (title.isEmpty()) isValid.append("Title is required.\n")
        if (address.isEmpty()) isValid.append("Address is required.\n")
        if (city.isEmpty()) isValid.append("City is required.\n")
        if (state == "Select State") isValid.append("State is required.\n")
        if (zipcode.isEmpty() || !Pattern.matches("\\d{6}", zipcode)) isValid.append("Valid Zipcode is required.\n")
        if (description.isEmpty()) isValid.append("Description is required.\n")
        if (price.isEmpty() || !Pattern.matches("\\d+", price)) isValid.append("Valid Price is required.\n")
        if (area.isEmpty() || !Pattern.matches("\\d+", area)) isValid.append("Valid Area is required.\n")
        if (amenities1.isEmpty()) isValid.append("At least one amenity is required.\n")
        if (amenities2.isEmpty()) isValid.append("At least one amenity is required.\n")
        if (amenities3.isEmpty()) isValid.append("At least one amenity is required.\n")


        if (isValid.isNotEmpty()) {
            Toast.makeText(this, isValid.toString(), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun openImagePicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            when (requestCode) {
                PHOTO_MAIN_REQUEST -> {
                    photoMainUri = imageUri
                    filenameTextView.text = getFileName(imageUri)
                    photoMainButton.text = photoUploadedText
                }
                PHOTO_1_REQUEST -> {
                    photo1Uri = imageUri
                    filenameTextView1.text = getFileName(imageUri)
                    photo1Button.text = photoUploadedText
                }
                PHOTO_2_REQUEST -> {
                    photo2Uri = imageUri
                    filenameTextView2.text = getFileName(imageUri)
                    photo2Button.text = photoUploadedText
                }
                PHOTO_3_REQUEST -> {
                    photo3Uri = imageUri
                    filenameTextView3.text = getFileName(imageUri)
                    photo3Button.text = photoUploadedText
                }
                PHOTO_4_REQUEST -> {
                    photo4Uri = imageUri
                    filenameTextView4.text = getFileName(imageUri)
                    photo4Button.text = photoUploadedText
                }
            }
        }
    }

    private fun getFileName(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val fileNameIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        cursor?.moveToFirst()
        val fileName = cursor?.getString(fileNameIndex!!)
        cursor?.close()
        return fileName ?: "Unknown file"
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, getFileName(uri))
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }



    private fun showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this).apply {
                setMessage("Loading...")
                setCancelable(false)
            }
        }
        if (!isFinishing) {
            progressDialog?.show()
        }
    }

    private fun hideLoadingDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }
    private fun compressAndResizeImage(uri: Uri): File {
        val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        // Resize the image if necessary (e.g., max width and height)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 800, 600, true) // Adjust as needed
        val file = File(cacheDir, getFileName(uri))
        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out) // Compress with quality
        }
        return file
    }

}
