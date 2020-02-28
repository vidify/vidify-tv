package com.glowapps.vidify

import android.os.Bundle
import androidx.fragment.app.FragmentActivity


class IntroActivity : FragmentActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity)
    }
}