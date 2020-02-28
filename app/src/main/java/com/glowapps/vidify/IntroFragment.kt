package com.glowapps.vidify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.OnboardingSupportFragment
import androidx.preference.PreferenceManager


class IntroFragment() : OnboardingSupportFragment() {
    private val introTitles: Array<String> = resources.getStringArray(R.array.intro_titles)
    private val introDescriptions: Array<String> = resources.getStringArray(R.array.intro_descriptions)

    override fun getPageCount(): Int {
        return introTitles.size
    }

    override fun getPageTitle(pageIndex: Int): CharSequence? {
        return introTitles[pageIndex]
    }

    override fun getPageDescription(pageIndex: Int): CharSequence? {
        return introDescriptions[pageIndex]
    }

    override fun onCreateForegroundView(p0: LayoutInflater, p1: ViewGroup): View? {
        return null
    }

    override fun onCreateBackgroundView(p0: LayoutInflater, p1: ViewGroup): View? {
        return null
    }

    override fun onCreateContentView(p0: LayoutInflater, p1: ViewGroup): View? {
        return null
    }

    override fun onFinishFragment() {
        super.onFinishFragment()
        // User has seen OnboardingSupportFragment, so mark our SharedPreferences
        // flag as completed so that we don't show our OnboardingSupportFragment
        // the next time the user launches the app.
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putBoolean(COMPLETED_ONBOARDING_PREF_NAME, true)
            apply()
        }
    }

    companion object {
        const val COMPLETED_ONBOARDING_PREF_NAME = "Onboarding Completed"
    }
}