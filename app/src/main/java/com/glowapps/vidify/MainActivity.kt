package com.glowapps.vidify

import android.os.Bundle

class MainActivity : TVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_fragment)
        window.setBackgroundDrawableResource(R.drawable.bg)
    }
}
