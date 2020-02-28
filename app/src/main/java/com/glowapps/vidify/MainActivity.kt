package com.glowapps.vidify

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceManager


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            // Check if we need to display our OnboardingSupportFragment
            if (!getBoolean(IntroFragment.COMPLETED_ONBOARDING_PREF_NAME, false)) {
                // The user hasn't seen the OnboardingSupportFragment yet, so show it
                println("Showing initial screen...")
                startActivity(Intent(this@MainActivity, IntroActivity::class.java))
            }
        }
    }
}
