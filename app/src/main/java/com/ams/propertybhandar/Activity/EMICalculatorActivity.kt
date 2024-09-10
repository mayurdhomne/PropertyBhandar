package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.pow

class EMICalculatorActivity : AppCompatActivity() {

    private lateinit var loanAmountEditText: TextInputEditText
    private lateinit var interestRateEditText: TextInputEditText
    private lateinit var loanTenureEditText: TextInputEditText
    private lateinit var emiAmountTextView: TextView
    private lateinit var calculateButton: MaterialButton
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emicalculator)

        loanAmountEditText = findViewById(R.id.loanAmountEditText)
        interestRateEditText = findViewById(R.id.interestRateEditText)
        loanTenureEditText = findViewById(R.id.loanTenureEditText)
        emiAmountTextView = findViewById(R.id.emiAmount)
        calculateButton = findViewById(R.id.calculateButton)
        backButton = findViewById(R.id.backButton)

        calculateButton.setOnClickListener {
            calculateEMI()
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
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

            emiAmountTextView.text = String.format("₹%.2f", emi)
        } else {
            emiAmountTextView.text = "Please fill all fields"
        }
    }
}
