package com.glowapps.vidify.model

import android.net.nsd.NsdServiceInfo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.ParseException

// TODO: move this to a data/ directory. Same for the other files.

// This structure holds all the information about the available devices, which will be shown to
// the user in cards, and then a connection will be established.
@Parcelize
data class Device(
    val name: String, val description: String, val cardImage: Int,
    val serviceInfo: NsdServiceInfo
) : Parcelable
