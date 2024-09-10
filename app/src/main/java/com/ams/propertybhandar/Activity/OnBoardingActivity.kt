package com.ams.propertybhandar.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ams.propertybhandar.Fragment.OnBoardingFragment1
import com.ams.propertybhandar.Fragment.OnBoardingFragment2
import com.ams.propertybhandar.Fragment.OnBoardingFragment3
import com.ams.propertybhandar.R

class OnBoardingActivity : AppCompatActivity() {

    private var currentFragmentIndex = 0

    private val fragments = listOf(
        OnBoardingFragment1(),
        OnBoardingFragment2(),
        OnBoardingFragment3()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        // Initialize the first onboarding fragment
        if (savedInstanceState == null) {
            showFragment(fragments[currentFragmentIndex])
        }
    }

    // Method to show a fragment
    private fun showFragment(fragment: Any) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment as Fragment)
            .commit()
    }

    // Method to go to the next page
    fun nextPage() {
        currentFragmentIndex++
        if (currentFragmentIndex < fragments.size) {
            showFragment(fragments[currentFragmentIndex])
        } else {
            // If there are no more fragments, finish onboarding
            finishOnboarding()
        }
    }

    // Method to skip the current onboarding page
    fun skipOnboarding() {
        if (currentFragmentIndex < fragments.size - 1) {
            currentFragmentIndex++
            showFragment(fragments[currentFragmentIndex])
        } else {
            // If already at the last fragment, finish onboarding
            finishOnboarding()
        }
    }

    // Method to finish the onboarding process
    fun finishOnboarding() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close the onboarding activity
    }
}
