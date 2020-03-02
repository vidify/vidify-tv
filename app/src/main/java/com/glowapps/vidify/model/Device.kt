package com.glowapps.vidify.model

import android.net.nsd.NsdServiceInfo
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// This structure holds all the information about the available devices, which will be shown to
// the user in cards, and then a connection will be established.
@Parcelize
data class Device(
    val name: String, val description: String, val cardImage: Int,
    val serviceInfo: NsdServiceInfo
) : Parcelable