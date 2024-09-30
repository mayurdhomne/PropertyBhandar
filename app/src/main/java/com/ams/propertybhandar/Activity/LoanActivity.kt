package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.R

class LoanActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        // Loan type buttons
        val landPlotLoan: LinearLayout = findViewById(R.id.LandPlotLoan)
        val homeLoan: LinearLayout = findViewById(R.id.HomeLoan)
        val constructionLoan: LinearLayout = findViewById(R.id.ConstructionLoan)
        val improvementLoan: LinearLayout = findViewById(R.id.ImprovementLoan)
        val topUpLoan: LinearLayout = findViewById(R.id.TopUpLoan)
        val preApprovedLoan: LinearLayout = findViewById(R.id.PreApprovedLoan)
        val pmayLoan: LinearLayout = findViewById(R.id.PmayLoan)
        val balanceTransferLoan: LinearLayout = findViewById(R.id.BalanceTransferLoan)
        val nriHomeLoan: LinearLayout = findViewById(R.id.NriHomeLoan)

        val feedbackButton: Button = findViewById(R.id.explore_btn)
        val right_arrow: CardView = findViewById(R.id.emi_calculator_card)
        val right_arrow2: CardView = findViewById(R.id.primer_calculator_card)
        val right_arrow3: CardView = findViewById(R.id.affordability_calculator_card)

        feedbackButton.setOnClickListener {
            // Check if the user is logged in
            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                // User is logged in, navigate to LoanApplicationActivity
                val intent = Intent(this@LoanActivity, LoanApplicationActivity::class.java)
                startActivity(intent)
            } else {
                // Show dialog to notify the user to log in first
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Attention Required")
                builder.setMessage("You need to be logged in to apply for a loan. Please log in to continue.")
                builder.setIcon(R.drawable.ic_warning) // Optional: Add a relevant icon if needed

                builder.setPositiveButton("Log in Now") { dialog, _ ->
                    // Navigate to LoginActivity
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    dialog.dismiss()
                }

                builder.setNegativeButton("Maybe Later") { dialog, _ ->
                    // Just dismiss the dialog
                    dialog.dismiss()
                }

                val dialog = builder.create()

                // Set the background of the dialog to the custom drawable with white background and rounded corners
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

                dialog.show()
            }
        }

        // Set up click listeners for loan types
        landPlotLoan.setOnClickListener {
            showLoanDetails("Land/Plot Loan", "A loan specifically for purchasing a plot of land for residential or investment purposes. This loan typically has stricter eligibility criteria and may require a larger down payment.", R.drawable.landplot)
        }

        homeLoan.setOnClickListener {
            showLoanDetails("Home Loan", "A loan for purchasing, constructing, or renovating a residential property. It is one of the most common types of loans, with flexible terms and interest rates.", R.drawable.homeloan)
        }

        constructionLoan.setOnClickListener {
            showLoanDetails("Construction Loan", "A short-term loan used to finance the construction of a new home or building. Funds are typically released in stages as the construction progresses.", R.drawable.construction)
        }

        improvementLoan.setOnClickListener {
            showLoanDetails("Improvement Loan", "This loan is designed to cover the costs of home improvements, repairs, or renovations. It can help homeowners enhance their property without exhausting their savings.", R.drawable.improvement)
        }

        topUpLoan.setOnClickListener {
            showLoanDetails("Top-Up Loan", "An additional loan that can be availed on an existing home loan. It’s used for personal or housing-related expenses and usually offers lower interest rates than personal loans.\n" +
                    "\n", R.drawable.top_up)
        }

        preApprovedLoan.setOnClickListener {
            showLoanDetails("Pre-Approved Loan", "A loan that is pre-approved based on the borrower's creditworthiness. It offers faster disbursal and easier processing as lenders have already assessed eligibility.", R.drawable.pre_approved)
        }

        pmayLoan.setOnClickListener {
            showLoanDetails("PMAY Loan", "Pradhan Mantri Awas Yojana Loan: A government-backed loan scheme aimed at providing affordable housing to urban and rural areas. Eligible borrowers receive interest subsidies under the PMAY scheme.", R.drawable.construction)
        }

        balanceTransferLoan.setOnClickListener {
            showLoanDetails("Balance Transfer Loan", "A facility that allows you to transfer an existing loan to another lender offering better interest rates or terms. It helps reduce the overall interest burden.", R.drawable.balance_transfer)
        }

        nriHomeLoan.setOnClickListener {
            showLoanDetails("NRI Home Loan", "A home loan specifically tailored for Non-Resident Indians (NRIs) to purchase, construct, or renovate residential properties in India. The loan terms may vary based on residency status and income.", R.drawable.nri_home_loan)
        }
        right_arrow.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, EMICalculatorActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

        }
        right_arrow2.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, LoanEligibilityActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

        }
        right_arrow3.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, HomeBudgetCalculatorActivity::class.java)
            startActivity(intent)

            // Initialize back_arrow ImageView and set click listener

        }
        val backArrow: ImageView = findViewById(R.id.back_button)
        backArrow.setOnClickListener {
            // Open Home when back arrow is clicked
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()  // Optional: Finish the current activity if you don't want to return to it


        }

    }
    // Function to show loan details in a dialog with image
    private fun showLoanDetails(title: String, details: String, imageResId: Int) {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_loan_details, null)

        // Create the dialog
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        // Find views in the custom layout
        val loanTitleTextView: TextView = dialogView.findViewById(R.id.loan_title)
        val loanDetailsTextView: TextView = dialogView.findViewById(R.id.loan_details)
        val loanImageView: ImageView = dialogView.findViewById(R.id.loanImageView)

        // Set the loan title, details, and image
        loanTitleTextView.text = title
        loanDetailsTextView.text = details
        loanImageView.setImageResource(imageResId)

        // Show the dialog
        val loanDetailsDialog = dialogBuilder.create()
        loanDetailsDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        loanDetailsDialog.show()
    }
}