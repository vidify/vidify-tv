package com.glowapps.vidify.util

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import com.glowapps.vidify.R

fun isTV(activity: Activity): Boolean {
    val uiModeManager = activity.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return uiModeManager.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION
}

// The background size depends on the type of device
fun getBackground(activity: Activity): Int {
    return if (isTV(activity)) { R.drawable.bg_tv } else { R.drawable.bg_mobile }
}
