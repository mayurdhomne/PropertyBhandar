package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.R
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.pow

class LoanEligibilityActivity : AppCompatActivity() {

    private lateinit var ageInput: TextInputEditText
    private lateinit var incomeInput: TextInputEditText
    private lateinit var emiInput: TextInputEditText
    private lateinit var interestRateInput: TextInputEditText
    private lateinit var tenureInput: TextInputEditText
    private lateinit var backButton: ImageView

    private lateinit var borrowAmountText: TextView
    private lateinit var payableAmountText: TextView
    private lateinit var monthlyEmiText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_eligibility)

        // Initialize views
        backButton = findViewById(R.id.backic)
        ageInput = findViewById(R.id.ageInput)
        incomeInput = findViewById(R.id.incomeInput)
        emiInput = findViewById(R.id.emiInput)
        interestRateInput = findViewById(R.id.interestRateInput)
        tenureInput = findViewById(R.id.tenureInput)

        borrowAmountText = findViewById(R.id.borrowAmount)
        payableAmountText = findViewById(R.id.payableAmountValue)
        monthlyEmiText = findViewById(R.id.monthlyEmiValue)


        val backButton: ImageView = findViewById(R.id.backic)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val calculateButton: CardView = findViewById(R.id.calculateButton)
        calculateButton.setOnClickListener {
            calculateLoanEligibility()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun calculateLoanEligibility() {
        val age = ageInput.text.toString().toIntOrNull() ?: 0
        val income = incomeInput.text.toString().toDoubleOrNull() ?: 0.0
        val emi = emiInput.text.toString().toDoubleOrNull() ?: 0.0
        val interestRate = interestRateInput.text.toString().toDoubleOrNull() ?: 0.0
        val tenure = tenureInput.text.toString().toIntOrNull() ?: 0

        if (age < 21) {
            borrowAmountText.text = "₹ 0"
            payableAmountText.text = "₹ 0"
            monthlyEmiText.text = "₹ 0"
            return
        }

        val maxLoanAmount = (income - emi) * 60 // Assuming 60 times the monthly surplus is the borrowing limit
        val monthlyInterestRate = interestRate / (12 * 100)
        val tenureMonths = tenure * 12

        // EMI calculation formula
        val emiValue = (maxLoanAmount * monthlyInterestRate * (1 + monthlyInterestRate).pow(tenureMonths)) /
                ((1 + monthlyInterestRate).pow(tenureMonths) - 1)

        // Total payable amount over the tenure
        val totalPayableAmount = emiValue * tenureMonths

        // Update UI
        borrowAmountText.text = "₹ %.2f".format(maxLoanAmount)
        payableAmountText.text = "₹ %.2f".format(totalPayableAmount)
        monthlyEmiText.text = "₹ %.2f".format(emiValue)
    }
}
