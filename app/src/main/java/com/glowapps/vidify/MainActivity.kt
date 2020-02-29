package com.glowapps.vidify

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        window.setBackgroundDrawableResource(R.drawable.bg)
    }
}
