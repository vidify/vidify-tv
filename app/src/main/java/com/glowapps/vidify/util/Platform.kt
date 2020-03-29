package com.glowapps.vidify.util

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration

fun isTV(activity: Activity): Boolean {
    val uiModeManager = activity.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return uiModeManager.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION
}
