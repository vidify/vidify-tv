package com.glowapps.vidify.nsd

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.glowapps.vidify.MainFragment

// Manages the services found and lost with NDS. Contains two callbacks, one called when a new
// service is found, and another called when a service is lost.
class DeviceDiscoveryListener(
    private val nsdManager: NsdManager,
    private val newService: (NsdServiceInfo) -> Unit,
    private val lostService: (NsdServiceInfo) -> Unit
) : NsdManager.DiscoveryListener {
    override fun onDiscoveryStarted(regType: String) {
        Log.d(MainFragment.TAG, "Service discovery started")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        Log.d(MainFragment.TAG, "Service discovery success: $service")
        when {
            service.serviceType != MainFragment.SERVICE_TYPE ->
                Log.d(MainFragment.TAG, "Unknown Service Type: " + service.serviceType)
            service.serviceName.contains(MainFragment.SERVICE_NAME) -> {
                Log.d(MainFragment.TAG, "Resolving service: ${MainFragment.SERVICE_NAME}")
                nsdManager.resolveService(service, DeviceResolveListener(newService))
            }
            else -> Log.d(MainFragment.TAG, "Name didn't match")
        }
    }

    override fun onServiceLost(lost: NsdServiceInfo) {
        Log.e(MainFragment.TAG, "Service lost: $lost")
        lostService(lost)
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.i(MainFragment.TAG, "Discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(
        serviceType: String,
        errorCode: Int
    ) {
        Log.e(MainFragment.TAG, "onStartDiscoveryFailed: $serviceType. Error code: $errorCode")
    }

    override fun onStopDiscoveryFailed(
        serviceType: String,
        errorCode: Int
    ) {
        Log.e(MainFragment.TAG, "onStopDiscoveryFailed: $serviceType. Error code: $errorCode")
    }
}