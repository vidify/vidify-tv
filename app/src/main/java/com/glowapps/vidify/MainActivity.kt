package com.glowapps.vidify

import android.os.Bundle
import com.glowapps.vidify.util.getBackground

class MainActivity : TVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(getBackground(this))
        setContentView(R.layout.main_fragment)
    }
}
