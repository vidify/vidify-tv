package com.glowapps.vidify

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class VideoPlayerActivity : FragmentActivity() {
    companion object {
        const val DEVICE_ARG = "device"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        window.setBackgroundDrawableResource(R.drawable.bg)
    }
}