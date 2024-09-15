package com.ams.propertybhandar.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ams.propertybhandar.Activity.OnBoardingActivity
import com.ams.propertybhandar.R

class OnBoardingFragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.onboarding_screen3, container, false)

        // Find the skip TextView
        val skipTextView: TextView = view.findViewById(R.id.tv_skip)

        // Set the skip button click listener
        skipTextView.setOnClickListener {
            (activity as? OnBoardingActivity)?.skipOnboarding()
        }

        // Find the next Button
        val nextButton: Button = view.findViewById(R.id.btn_next)

        // Set the next button click listener
        nextButton.setOnClickListener {
            (activity as? OnBoardingActivity)?.finishOnboarding()
        }

        return view
    }
}