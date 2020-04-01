package com.glowapps.vidify.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.glowapps.vidify.R

fun share(activity: Activity) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(
            Intent.EXTRA_SUBJECT,
            activity.getString(R.string.app_name)
        )
        putExtra(
            Intent.EXTRA_TEXT,
            activity.getString(R.string.playstore_link)
        )
        type = "text/plain"
    }
    startActivity(activity, intent, null)
}

fun openURL(context: Context, url: String) {
    val uriUrl: Uri = Uri.parse(url)
    val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
    startActivity(context, launchBrowser, null)
}
