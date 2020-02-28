package com.glowapps.vidify

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        // window.setBackgroundDrawableResource(R.drawable.bg)
        /*
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            // Check if we need to display our OnboardingSupportFragment
            if (!getBoolean(OnboardingFragment.COMPLETED_ONBOARDING_PREF_NAME, false)) {
                // The user hasn't seen the OnboardingSupportFragment yet, so show it
                println("Showing initial screen...")
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
            }
        }
         */
    }
}
