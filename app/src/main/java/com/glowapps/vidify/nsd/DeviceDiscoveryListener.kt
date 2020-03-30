package com.glowapps.vidify.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.glowapps.vidify.tv.MainTVFragment

// Manages the services found and lost with NDS. Contains two callbacks, one called when a new
// service is found, and another called when a service is lost.
class DeviceDiscoveryListener(
    private val nsdManager: NsdManager,
    private val newService: (NsdServiceInfo) -> Unit,
    private val lostService: (NsdServiceInfo) -> Unit
) : NsdManager.DiscoveryListener {
    override fun onDiscoveryStarted(regType: String) {
        Log.i(MainTVFragment.TAG, "Service discovery started")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        Log.i(MainTVFragment.TAG, "Service discovery success: $service")
        when {
            service.serviceType != MainTVFragment.SERVICE_TYPE ->
                Log.d(MainTVFragment.TAG, "Unknown Service Type: " + service.serviceType)
            service.serviceName.contains(MainTVFragment.SERVICE_NAME) -> {
                Log.d(MainTVFragment.TAG, "Resolving service: ${MainTVFragment.SERVICE_NAME}")
                nsdManager.resolveService(service, DeviceResolveListener(newService))
            }
            else -> Log.d(MainTVFragment.TAG, "Name didn't match")
        }
    }

    override fun onServiceLost(lost: NsdServiceInfo) {
        Log.e(MainTVFragment.TAG, "Service lost: $lost")
        lostService(lost)
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.i(MainTVFragment.TAG, "Discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(MainTVFragment.TAG, "onStartDiscoveryFailed: $serviceType. Error code: $errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(MainTVFragment.TAG, "onStopDiscoveryFailed: $serviceType. Error code: $errorCode")
    }
}