package com.glowapps.vidify.tv

import android.os.Bundle
import com.glowapps.vidify.R

class MainTVActivity : BaseTVActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.drawable.background)
        setContentView(R.layout.tv_main_fragment)
    }
}
