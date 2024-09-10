package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ams.propertybhandar.R
import com.google.android.material.textfield.TextInputEditText

class HomeBudgetCalculatorActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var savingsInput: TextInputEditText
    private lateinit var emiInput: TextInputEditText
    private lateinit var loanTenureInput: TextInputEditText
    private lateinit var budgetRangeValue: TextView
    private lateinit var viewPropertiesButton: Button

    private var minBudget: Double = 0.0
    private var maxBudget: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_budget_calculator)

        backButton = findViewById(R.id.backButton)
        savingsInput = findViewById(R.id.savingsInput)
        emiInput = findViewById(R.id.emiInput)
        loanTenureInput = findViewById(R.id.loanTenureInput)
        budgetRangeValue = findViewById(R.id.budgetRangeValue)
        viewPropertiesButton = findViewById(R.id.viewPropertiesButton)

        backButton.setOnClickListener {
            onBackPressed()
        }

        savingsInput.addTextChangedListener(textWatcher)
        emiInput.addTextChangedListener(textWatcher)
        loanTenureInput.addTextChangedListener(textWatcher)

        viewPropertiesButton.setOnClickListener {
            val intent = Intent(this, PropertyListActivity::class.java).apply {
                putExtra("min_budget", minBudget)
                putExtra("max_budget", maxBudget)
            }
            startActivity(intent)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            calculateBudget()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    @SuppressLint("SetTextI18n")
    private fun calculateBudget() {
        val savings = savingsInput.text.toString().toDoubleOrNull() ?: 0.0
        val emi = emiInput.text.toString().toDoubleOrNull() ?: 0.0
        val loanTenure = loanTenureInput.text.toString().toIntOrNull() ?: 0

        // Simple budget calculation
        minBudget = savings + (emi * loanTenure * 12 * 0.8)  // Minimum budget (80% of max)
        maxBudget = savings + (emi * loanTenure * 12)        // Maximum budget

        // Update the budget range text
        budgetRangeValue.text = "₹ ${minBudget.toInt()} - ₹ ${maxBudget.toInt()}"
    }
}
