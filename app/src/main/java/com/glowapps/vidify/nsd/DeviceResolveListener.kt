package com.glowapps.vidify.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.glowapps.vidify.MainFragment

// The custom resolve listener is simple for now. It will just call a set function when the
// resolve function was successful.
class DeviceResolveListener(private val callback: (NsdServiceInfo) -> Unit) :
    NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        Log.e(MainFragment.TAG, "Resolve failed: $errorCode")
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        Log.i(MainFragment.TAG, "Resolve succeeded: $serviceInfo")

        callback(serviceInfo)
    }
}