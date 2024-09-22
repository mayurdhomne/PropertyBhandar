package com.ams.propertybhandar.Activity

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.Domin.NetworkClient
import com.ams.propertybhandar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.Calendar

class LoanApplicationActivity : AppCompatActivity() {

    private lateinit var networkClient: NetworkClient
    private var photoUri: Uri? = null
    private var documentUri: Uri? = null

    private lateinit var uploadPhotoBtn: MaterialButton
    private lateinit var uploadDocumentBtn: MaterialButton
    private lateinit var photoFileName: MaterialTextView
    private lateinit var documentFileName: MaterialTextView
    private lateinit var submitBtn: MaterialButton
    private lateinit var backButton: ImageView
    private lateinit var loanTypeSpinner: Spinner
    private lateinit var stateSpinner: Spinner
    private lateinit var dobEditText: TextInputEditText
    private var customLoadingDialog: CustomLoadingDialog? = null

    private val pickPhotoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                photoUri = result.data?.data
                photoFileName.text = getFileName(photoUri)
            }
        }

    private val pickDocumentLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                documentUri = result.data?.data
                documentFileName.text = getFileName(documentUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_application)

        networkClient = NetworkClient(this)
        val isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)

        if (!isLoggedIn) {
            showLoginRequiredDialog()
        }

        // Initialize UI components
        backButton = findViewById(R.id.backButton)
        uploadPhotoBtn = findViewById(R.id.uploadPhotoBtn)
        uploadDocumentBtn = findViewById(R.id.uploadDocumentBtn)
        photoFileName = findViewById(R.id.photoFileName)
        documentFileName = findViewById(R.id.documentFileName)
        submitBtn = findViewById(R.id.submitBtn)
        loanTypeSpinner = findViewById(R.id.loanTypeSpinner)
        stateSpinner = findViewById(R.id.stateSpinner)
        dobEditText = findViewById(R.id.dob)

        setupSpinner(loanTypeSpinner, resources.getStringArray(R.array.loan_types))
        setupSpinner(stateSpinner, resources.getStringArray(R.array.indian_states))

        backButton.setOnClickListener {
            onBackPressed()
        }

        uploadPhotoBtn.setOnClickListener {
            pickPhoto()
        }

        uploadDocumentBtn.setOnClickListener {
            pickDocument()
        }

        submitBtn.setOnClickListener {
            submitLoanApplication()
        }

        dobEditText.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun setupSpinner(spinner: Spinner, items: Array<String>) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            items
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            dobEditText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun collectUserInput(): Map<String, String>? {
        val firstName = findViewById<TextInputEditText>(R.id.firstName).text.toString()
        val lastName = findViewById<TextInputEditText>(R.id.lastName).text.toString()
        val email = findViewById<TextInputEditText>(R.id.email).text.toString()
        val dob = findViewById<TextInputEditText>(R.id.dob).text.toString()
        val phone = findViewById<TextInputEditText>(R.id.phone).text.toString()
        val address = findViewById<TextInputEditText>(R.id.address).text.toString()
        val pinCode = findViewById<TextInputEditText>(R.id.pinCode).text.toString()
        val city = findViewById<TextInputEditText>(R.id.city).text.toString()
        val state = stateSpinner.selectedItem.toString()
        val salary = findViewById<TextInputEditText>(R.id.salary).text.toString()
        val loanType = loanTypeSpinner.selectedItem.toString()
        val loanAmount = findViewById<TextInputEditText>(R.id.loanAmount).text.toString()
        val tenure = findViewById<TextInputEditText>(R.id.tenure).text.toString()

        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || dob.isBlank() ||
            phone.isBlank() || address.isBlank() || pinCode.isBlank() || city.isBlank() ||
            state.isBlank() || salary.isBlank() || loanType.isBlank() || loanAmount.isBlank() ||
            tenure.isBlank()) {
            return null
        }

        return mapOf(
            "fname" to firstName,
            "lname" to lastName,
            "email" to email,
            "dob" to dob,
            "phone" to phone,
            "address" to address,
            "pin_code" to pinCode,
            "city" to city,
            "state" to getStateCode(state),
            "salary" to salary,
            "loan_type" to getLoanTypeCode(loanType),
            "loan_amt" to loanAmount,
            "down_pmt" to tenure
        )
    }


    private fun getStateCode(stateName: String): String {
        return when (stateName) {
            "Andaman and Nicobar Islands" -> "AN"
            "Andhra Pradesh" -> "AP"
            "Arunachal Pradesh" -> "AR"
            "Assam" -> "AS"
            "Bihar" -> "BR"
            "Chandigarh" -> "CG"
            "Chhattisgarh" -> "CH"
            "Dadra and Nagar Haveli" -> "DN"
            "Daman and Diu" -> "DD"
            "Delhi" -> "DL"
            "Goa" -> "GA"
            "Gujarat" -> "GJ"
            "Haryana" -> "HR"
            "Himachal Pradesh" -> "HP"
            "Jammu and Kashmir" -> "JK"
            "Jharkhand" -> "JH"
            "Karnataka" -> "KA"
            "Kerala" -> "KL"
            "Ladakh" -> "LA"
            "Lakshadweep" -> "LD"
            "Madhya Pradesh" -> "MP"
            "Maharashtra" -> "MH"
            "Manipur" -> "MN"
            "Meghalaya" -> "ML"
            "Mizoram" -> "MZ"
            "Nagaland" -> "NL"
            "Odisha" -> "OR"
            "Puducherry" -> "PY"
            "Punjab" -> "PB"
            "Rajasthan" -> "RJ"
            "Sikkim" -> "SK"
            "Tamil Nadu" -> "TN"
            "Telangana" -> "TS"
            "Tripura" -> "TR"
            "Uttar Pradesh" -> "UP"
            "Uttarakhand" -> "UK"
            "West Bengal" -> "WB"
            else -> ""
        }
    }

    private fun getLoanTypeCode(loanTypeName: String): String {
        return when (loanTypeName) {
            "Home Loan" -> "hl"
            "Home Construction Loan" -> "hcl"
            "Land or Plot Loan" -> "lpl"
            "Home Improvement and Extension Loan" -> "hiel"
            "Top-Up Home Loan" -> "tuhl"
            "Pre-Approved Home Loan" -> "pahl"
            "PMAY Loan" -> "pmay"
            "Balance Transfer Home Loan" -> "bthl"
            "NRI Home Loans" -> "nri"
            else -> ""
        }
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoLauncher.launch(intent)
    }

    private fun pickDocument() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
        }
        pickDocumentLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri?): String {
        uri?.let {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                if (nameIndex != -1) {
                    cursor.moveToFirst()
                    return cursor.getString(nameIndex)
                }
            }
            return uri.path?.substringAfterLast('/') ?: "unknown_file"
        }
        return "No file selected"
    }

    private fun uriToFile(uri: Uri?): File? {
        uri?.let {
            val fileName = getFileName(uri)
            val tempFile = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        }
        return null
    }

    private fun submitLoanApplication() {
        showLoadingDialog()
        val userInput = collectUserInput()
        if (userInput == null) {
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile = uriToFile(photoUri)
        val documentFile = uriToFile(documentUri)

        val requestBody = buildMultipartBody(userInput, photoFile, documentFile)
        val accessToken = networkClient.getAccessToken()

        Log.d("LoanApplication", "Request Body: ${requestBody}")

        val request = Request.Builder()
            .url("https://propertybhandar.com/api/loan_application/")
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        networkClient.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleNetworkError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                handleNetworkResponse(response)
            }
        })
    }

    private fun buildMultipartBody(
        userInput: Map<String, String>,
        photoFile: File?,
        documentFile: File?
    ): RequestBody {
        val formBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

        userInput.forEach { (key, value) ->
            formBodyBuilder.addFormDataPart(key, value)
        }

        photoFile?.let {
            formBodyBuilder.addFormDataPart(
                "photo",
                it.name,
                it.asRequestBody(getMimeType(it).toMediaTypeOrNull())
            )
            Log.d("LoanApplication", "Added photo file: ${it.name}, MIME type: ${getMimeType(it)}")
        }

        documentFile?.let {
            formBodyBuilder.addFormDataPart(
                "doc_photo",
                it.name,
                it.asRequestBody(getMimeType(it).toMediaTypeOrNull())
            )
            Log.d("LoanApplication", "Added document file: ${it.name}, MIME type: ${getMimeType(it)}")
        }

        return formBodyBuilder.build()
    }

    private fun handleNetworkError(exception: IOException) {
        runOnUiThread {
            Log.e("LoanApplication", "Network request failed: ${exception.message}", exception)
            Toast.makeText(this@LoanApplicationActivity, "Failed to submit application", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNetworkResponse(response: Response) {
        runOnUiThread {
            if (response.isSuccessful) {
                hideLoadingDialog()
                Toast.makeText(this@LoanApplicationActivity, "Application submitted successfully", Toast.LENGTH_SHORT).show()
            } else {
                hideLoadingDialog()
                val responseBodyString = response.body?.string() ?: ""
                Log.e("LoanApplication", "Response not successful: $responseBodyString")
                Toast.makeText(this@LoanApplicationActivity, "Failed to submit application", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getMimeType(file: File): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    override fun onDestroy() {
        super.onDestroy()
        photoUri?.let { uriToFile(it)?.delete() }
        documentUri?.let { uriToFile(it)?.delete() }
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
    private fun showLoginRequiredDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_required, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        // Customize the dialog UI elements
        val titleTextView: TextView = dialogView.findViewById(R.id.dialogTitle)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialogMessage)
        val loginButton: CardView = dialogView.findViewById(R.id.btnLoginNow)
        val cancelButton: CardView = dialogView.findViewById(R.id.btnMaybeLater)

        titleTextView.text = "Login Required"
        messageTextView.text = "You need to be logged in to add a property."

        loginButton.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // Customize the dialog appearance (optional)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        // Make the dialog non-cancelable (no cancel button or back press)

        dialog.setCanceledOnTouchOutside(false)

        dialog.show()
    }
}
