package com.ams.propertybhandar.Fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ams.propertybhandar.Activity.OnBoardingActivity
import com.ams.propertybhandar.R


class OnBoardingFragment2 : Fragment(R.layout.onboarding_screen2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = view.findViewById<Button>(R.id.btn_next)
        nextButton.setOnClickListener {
            (activity as OnBoardingActivity).nextPage()
        }

        val skipTextView = view.findViewById<TextView>(R.id.tv_skip)
        skipTextView.setOnClickListener {
            (activity as OnBoardingActivity).skipOnboarding()
        }

    }
}
