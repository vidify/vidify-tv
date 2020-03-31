package com.glowapps.vidify.nsd

import android.app.Activity
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

// Abstraction system to manage the services found and lost with NDS. Contains two callbacks, one
// called when a new service is found, and another called when a service is lost.
class DeviceDiscoverySystem(
    private val activity: Activity,
    private val foundService: (NsdServiceInfo) -> Unit,
    private val lostService: (NsdServiceInfo) -> Unit
) : NsdManager.DiscoveryListener {

    companion object {
        const val TAG = "DeviceDiscoverySystem"
        const val SERVICE_TYPE = "_vidify._tcp."
        const val SERVICE_NAME = "vidify"
        const val SERVICE_PROTOCOL = NsdManager.PROTOCOL_DNS_SD  // DNS-based service discovery
    }

    private var nsdManager: NsdManager = activity.getSystemService(Context.NSD_SERVICE) as NsdManager

    fun start() {
        Log.i(TAG, "Starting NSD discovery")
        nsdManager.discoverServices(
            SERVICE_TYPE,
            SERVICE_PROTOCOL,
            this
        )
    }

    fun stop() {
        Log.i(TAG, "Stopping NSD discovery")
        nsdManager.stopServiceDiscovery(this)
    }

    override fun onDiscoveryStarted(regType: String) {
        Log.i(TAG, "Service discovery started: $regType")
    }

    override fun onServiceFound(service: NsdServiceInfo) {
        Log.i(TAG, "Service discovery success: ${service.serviceName}")
        when {
            service.serviceName.contains(SERVICE_NAME) -> {
                Log.i(TAG, "Resolving service: $SERVICE_NAME")
                nsdManager.resolveService(service, DeviceResolveListener())
            }
            service.serviceType != SERVICE_TYPE ->
                Log.e(TAG, "Unknown Service Type: ${service.serviceType}")
            else -> Log.e(TAG, "Name didn't match")
        }
    }

    override fun onServiceLost(lost: NsdServiceInfo) {
        Log.e(TAG, "Service lost: ${lost.serviceName}")
        lostService(lost)
    }

    override fun onDiscoveryStopped(serviceType: String) {
        Log.i(TAG, "Discovery stopped: $serviceType")
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(TAG, "onStartDiscoveryFailed: $serviceType. Error code: $errorCode")
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        Log.e(TAG, "onStopDiscoveryFailed: $serviceType. Error code: $errorCode")
    }

    // The custom resolve listener is simple for now. It will just call a set function when the
    // resolve function was successful.
    private inner class DeviceResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve succeeded: ${serviceInfo.serviceName}")

            foundService(serviceInfo)
        }
    }
}