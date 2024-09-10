package com.ams.propertybhandar.Activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ams.propertybhandar.R

class ServicesActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val feedbackButton: Button = findViewById(R.id.feedback_btn)
        val backButton: ImageView = findViewById(R.id.backic)


        backButton.setOnClickListener {
            onBackPressed()
        }

        feedbackButton.setOnClickListener {
            // Create an intent to start the ContactUsActivity
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)

        }

        // Find the CardViews and set click listeners
        findViewById<CardView>(R.id.cardLeaseAgreement).setOnClickListener {
            // Show the popup card for Lease Agreement
            showPopup(R.drawable.ic_lease_kt, "Lease Agreement", "A legal contract between a lessor and lessee Constitutes all requisites when a lessor engages with a lessee Allows the lessee to rent a property for a certain period.")
        }

        findViewById<CardView>(R.id.cardDocumentReview).setOnClickListener {
            // Show the popup card for Document Review
            showPopup(R.drawable.ic_documentr_kt, "Document Review", "Recommended for property buyers. Analyses property documents and compliance issues.")
        }

        findViewById<CardView>(R.id.cardSaleAgreement).setOnClickListener {
            // Show the popup card for Sale Agreement
            showPopup(R.drawable.ic_sale_kt, "Sale Agreement", "Includes all the regulations about the sale of a property Executed by a developer or a seller Constitutes all financial terms.")
        }

        findViewById<CardView>(R.id.cardDueDiligence).setOnClickListener {
            // Show the popup card for Comprehensive Due Diligence
            showPopup(R.drawable.ic_due_kt, "Comprehensive Due Diligence", "Analyses the property documents and compliance issues Verifies title search and mortgage checks Used for property valuation reports and verifying documents.")
        }

        findViewById<CardView>(R.id.cardPropertyComplaints).setOnClickListener {
            // Show the popup card for Property Complaints
            showPopup(R.drawable.ic_propertyc_kt, "Property Complaints", "Includes assessment of the complaint and the best route to take Sends legal letter to the Developer / Seller for refunds / Settlement Files case in appropriate courts - RERA, Consumer, NCLT, Arbitration.")
        }

        findViewById<CardView>(R.id.cardLitigation).setOnClickListener {
            // Show the popup card for Litigation Search
            showPopup(R.drawable.ic_litigation_kt, "Litigation Search", "Refers to actions contested in court Investigates charges levied on property sellers Deals with active, pending and disposed off cases.")
        }

        findViewById<CardView>(R.id.cardTitleSearch).setOnClickListener {
            // Show the popup card for Title Search
            showPopup(R.drawable.ic_title_kt, "Title Search", "Determines Various facets of a property history Helps In assessing the present titleholder Examines public records to confirm ownership of property.")
        }

        findViewById<CardView>(R.id.cardBuyOur).setOnClickListener {
            // Show the popup card for Buy
            showPopup(R.drawable.ic_buy_our_kt, "Buy", "Explore the legal intricacies of leasing agreements, ensuring all necessary requisites are met for a seamless lessor-lessee engagement, enabling the lessee to rent a property for a defined period.")
        }

        findViewById<CardView>(R.id.cardLoanOur).setOnClickListener {
            // Show the popup card for Loan
            showPopup(R.drawable.ic_loan_our_kt, "Loan", "Specifically tailored for property buyers, our loan service provides comprehensive analysis of property documents and compliance issues, ensuring a smooth and secure borrowing process.")
        }

        findViewById<CardView>(R.id.cardSellOur).setOnClickListener {
            // Show the popup card for Sell
            showPopup(R.drawable.ic_sell_our_kt, "Sell", "Encompassing all regulations pertaining to property sales, our sell service is managed by experienced developers or sellers, ensuring all financial terms are meticulously addressed.")
        }

        findViewById<CardView>(R.id.cardSaleAgreementOur).setOnClickListener {
            // Show the popup card for Sale Agreement
            showPopup(R.drawable.ic_sale_agreement_our_kt, "Sale Agreement", "Our Sale Agreement service meticulously analyzes property documents and compliance issues, conducts thorough title searches and mortgage checks, and is instrumental in producing property valuation reports and document verification.")
        }

        findViewById<CardView>(R.id.cardBuySellPropertyQuestionsOur).setOnClickListener {
            // Show the popup card for Buy/Sell Property Questions
            showPopup(R.drawable.ic_property_question_our_kt, "Buy/Sell Property Questions", "Our Sale Agreement service meticulously analyzes property documents and compliance issues, conducts thorough title searches and mortgage checks, and is instrumental in producing property valuation reports and document verification.")
        }
    }

    // Function to show the popup card
    private fun showPopup(imageResId: Int, title: String, description: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_popup) // Use the popup layout you created

        // Set the image, title, and description in the popup
        val imageView: ImageView = dialog.findViewById(R.id.popupImage)
        imageView.setImageResource(imageResId)

        val titleView: TextView = dialog.findViewById(R.id.popupTitle)
        titleView.text = title

        val descriptionView: TextView = dialog.findViewById(R.id.popupDescription)
        descriptionView.text = description

        // Find and set up the close button
        val closeButton: ImageView = dialog.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss() // Close the popup when the button is clicked
        }

        // Show the popup dialog
        dialog.show()
    }
}
