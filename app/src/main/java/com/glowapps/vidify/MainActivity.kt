package com.glowapps.vidify

import android.os.Bundle

class MainActivity : TVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.drawable.background)
        setContentView(R.layout.main_fragment)
    }
}
