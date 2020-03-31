package com.glowapps.vidify.nsd

import android.net.nsd.NsdServiceInfo
import com.glowapps.vidify.R
import java.util.*

// Obtaining basic attributes from a NsdServiceInfo structure to display them in cards

fun getTitle(service: NsdServiceInfo): String {
    return service.serviceName
}

fun getDescription(service: NsdServiceInfo): String {
    // By default, the description is the API. If it isn't found, the OS name is used.
    return when {
        service.attributes.containsKey("api") -> service.attributes["api"]!!.toString(Charsets.UTF_8)
        service.attributes.containsKey("os") -> service.attributes["os"]!!.toString(Charsets.UTF_8)
        else -> "Unknown device"
    }
}

fun getImage(service: NsdServiceInfo): Int {
    // The image is obtained with the OS attribute.
    return when (service.attributes["os"]?.toString(Charsets.UTF_8)?.toUpperCase(Locale.ROOT)) {
        "LINUX" -> R.drawable.os_linux
        "MACOS" -> R.drawable.os_macos
        "WINDOWS" -> R.drawable.os_windows
        "BSD" -> R.drawable.os_bsd
        else -> R.drawable.os_unknown
    }
}