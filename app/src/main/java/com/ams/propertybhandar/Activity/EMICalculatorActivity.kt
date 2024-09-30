package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ams.propertybhandar.R
import com.ams.propertybhandar.model.MonthlyEMI
import com.ams.propertybhandar.model.MonthlyEMIAdaper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

class EMICalculatorActivity : AppCompatActivity() {

    private lateinit var loanAmountEditText: TextInputEditText
    private lateinit var interestRateEditText: TextInputEditText
    private lateinit var loanTenureEditText: TextInputEditText
    private lateinit var emiAmountTextView: TextView // For Estimated EMI
    private lateinit var monthlyEMITextView: TextView // For Monthly EMI in summary
    private lateinit var totalInterestTextView: TextView
    private lateinit var totalPaymentTextView: TextView
    private lateinit var calculateButton: MaterialButton
    private lateinit var resetButton: MaterialButton
    private lateinit var backButton: ImageView
    private lateinit var monthlyEMIRecylerView: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var shareDetailsButton: MaterialButton
    private lateinit var pieChartHint: TextView
    private lateinit var monthlyBreakdown: List<MonthlyEMI>
    private lateinit var monthlyEMIAdaper: MonthlyEMIAdaper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emicalculator)

        loanAmountEditText = findViewById(R.id.loanAmountEditText)
        interestRateEditText = findViewById(R.id.interestRateEditText)
        loanTenureEditText = findViewById(R.id.loanTenureEditText)
        emiAmountTextView = findViewById(R.id.emiAmount) // Estimated EMI TextView
        monthlyEMITextView = findViewById(R.id.monthlyEMI)
        totalInterestTextView = findViewById(R.id.totalInterest)
        totalPaymentTextView = findViewById(R.id.totalPayment)
        calculateButton = findViewById(R.id.calculateButton)
        resetButton = findViewById(R.id.resetButton)
        backButton = findViewById(R.id.backButton)
        monthlyEMIRecylerView = findViewById(R.id.monthlyEMIRecylerView)
        pieChart = findViewById(R.id.pieChart)
        shareDetailsButton = findViewById(R.id.shareDetailsButton)
        pieChartHint = findViewById(R.id.pieChartHint)
        monthlyBreakdown = emptyList() // Initialize as an empty list

        monthlyEMIAdaper = MonthlyEMIAdaper(monthlyBreakdown) // Initialize adapter

        monthlyEMIRecylerView.layoutManager = LinearLayoutManager(this)
        monthlyEMIRecylerView.adapter = monthlyEMIAdaper

        calculateButton.setOnClickListener {
            calculateEMI()
        }

        resetButton.setOnClickListener {
            resetFields()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }

        shareDetailsButton.setOnClickListener { // Set the click listener
            shareEMIdetails()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n", "NotifyDataSetChanged")
    private fun calculateEMI() {
        val principal = loanAmountEditText.text.toString().toDoubleOrNull()
        val annualInterestRate = interestRateEditText.text.toString().toDoubleOrNull()
        val tenureInYears = loanTenureEditText.text.toString().toIntOrNull()

        if (principal != null && annualInterestRate != null && tenureInYears != null) {
            val monthlyInterestRate = (annualInterestRate / 12) / 100
            val numberOfMonths = tenureInYears * 12

            val emi = if (monthlyInterestRate != 0.0) {
                (principal * monthlyInterestRate * (1 + monthlyInterestRate).pow(numberOfMonths)) /
                        ((1 + monthlyInterestRate).pow(numberOfMonths) - 1)
            } else {
                principal / numberOfMonths
            }

            val totalInterest = (emi * numberOfMonths) - principal
            val totalPayment = totalInterest + principal

            // Update UI elements - WITHOUT decimal points
            val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

// Format and set the text with proper commas
            emiAmountTextView.text = "₹" + numberFormat.format(emi.toInt()) // Estimated EMI
            monthlyEMITextView.text = "₹" + numberFormat.format(emi.toInt())
            totalInterestTextView.text = "₹" + numberFormat.format(totalInterest.toInt())
            totalPaymentTextView.text = "₹" + numberFormat.format(totalPayment.toInt())

            // Generate monthly breakdown
            monthlyBreakdown = generateMonthlyBreakdown(principal, monthlyInterestRate, numberOfMonths, emi)

            // Update the adapter with the new monthly breakdown
            monthlyEMIAdaper.updateMonthlyEMIList(monthlyBreakdown)
            monthlyEMIAdaper.notifyDataSetChanged() // Notify the adapter of data change
            monthlyEMIRecylerView.visibility = View.VISIBLE // Show the RecyclerView

            setupPieChart(principal, totalInterest)
            pieChartHint.visibility = View.GONE
        } else {
            emiAmountTextView.text = "Please fill all fields" // Error message if any field is empty
            pieChartHint.visibility = View.VISIBLE
        }
    }

    private fun setupPieChart(principal: Double, totalInterest: Double) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(principal.toFloat(), "Principal"))
        entries.add(PieEntry(totalInterest.toFloat(), "Interest"))

        val dataSet = PieDataSet(entries, "Loan Breakdown")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList() // Customize colors

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false // Hide description
        pieChart.centerText = "Loan Breakdown" // Set center text
        pieChart.animateY(1000) // Add animation
        pieChart.invalidate() // Refresh chart
    }


    @SuppressLint("SetTextI18n")
    private fun resetFields() {
        loanAmountEditText.text?.clear()
        interestRateEditText.text?.clear()
        loanTenureEditText.text?.clear()

        emiAmountTextView.text = "₹0" // Estimated EMI
        monthlyEMITextView.text = "₹0"
        totalInterestTextView.text = "₹0"
        totalPaymentTextView.text = "₹0"

        // Clear the monthly breakdown and RecyclerView
        monthlyBreakdown = emptyList()
        monthlyEMIRecylerView.adapter = null
        monthlyEMIRecylerView.visibility = View.GONE
    }


    private fun generateMonthlyBreakdown(principal: Double, monthlyInterestRate: Double, numberOfMonths: Int, emi: Double): List<MonthlyEMI> {
        val breakdown = mutableListOf<MonthlyEMI>()
        var outstandingBalance = principal

        for (month in 1..numberOfMonths) {
            val interestComponent = outstandingBalance * monthlyInterestRate
            val principalComponent = emi - interestComponent
            outstandingBalance -= principalComponent
            breakdown.add(MonthlyEMI(month, principalComponent, interestComponent, outstandingBalance))
        }

        return breakdown
    }

    private fun generateShareableContent(): String {
        val content = StringBuilder()
        content.append("EMI Calculation Summary\n\n")
        content.append("Loan Amount: ₹${loanAmountEditText.text.toString()}\n")
        content.append("Interest Rate: ${interestRateEditText.text.toString()}%\n")
        content.append("Loan Tenure: ${loanTenureEditText.text.toString()} years\n")
        content.append("\n") // Adding a line break for clarity
        content.append("Monthly EMI: ₹${monthlyEMITextView.text}\n")
        content.append("Total Interest Payable: ₹${totalInterestTextView.text}\n")
        content.append("Total Amount Payable (Principal + Interest): ₹${totalPaymentTextView.text}\n")
        content.append("\nThis summary was generated using the PropertyBhandar EMI Calculator.\n")
        content.append("For further assistance, please contact us.\n")
        return content.toString()
    }


    private fun shareEMIdetails() {
        val shareText = generateShareableContent()

        val bitmap = getBitmapFromView(pieChart)

        if (bitmap != null) {
            val imageUri = saveImageToCache(bitmap, "emi_chart.jpg") // Save as JPG

            if (imageUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/jpeg" // Set MIME type to JPEG

                // Draw a white background behind the pie chart for better visibility
                val whiteBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
                val canvas = Canvas(whiteBitmap)
                canvas.drawColor(Color.WHITE) // White background for a cleaner look
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                // Attach both the image and text
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Start share intent
                startActivity(Intent.createChooser(shareIntent, "Share EMI Details"))
            } else {
                Toast.makeText(this, "Failed to save chart image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Failed to create chart image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun saveImageToCache(bitmap: Bitmap, fileName: String): Uri? {
        try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val stream: OutputStream = FileOutputStream("$cachePath/$fileName")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) // Compress as JPEG for quality
            stream.close()
            val imagePath = File(cachePath, fileName)
            return FileProvider.getUriForFile(
                this,
                "com.ams.propertybhandar.fileprovider",
                imagePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}