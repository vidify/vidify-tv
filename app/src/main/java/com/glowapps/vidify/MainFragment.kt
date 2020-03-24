package com.glowapps.vidify

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*
import com.glowapps.vidify.presenter.CardPresenter


class MainFragment : VerticalGridSupportFragment() {
    companion object {
        const val TAG = "MainFragment"
        const val SERVICE_TYPE = "_vidify._tcp."
        const val SERVICE_NAME = "vidify"
        private const val NUM_COLUMNS = 4
    }

    private lateinit var cardAdapter: ArrayObjectAdapter

    private var nsdManager: NsdManager? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting fullscreen and loading the activity layout to play the videos
        title = getString(R.string.app_name)

        // Including the presenter to manage the grid layout itself.
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)

        // Setting the card adapter, an interface used to manage the cards displayed in the
        // grid view.
        cardAdapter = ArrayObjectAdapter(CardPresenter())
        adapter = cardAdapter

        // Loading the grid state, trying to reuse the previous one
        if (savedInstanceState == null) {
            prepareEntranceTransition()
        }
        startEntranceTransition()
    }

    override fun onStart() {
        // Initializing the network service discovery
        nsdManager = activity!!.getSystemService(Context.NSD_SERVICE) as NsdManager

        // Initializing the callbacks for the items
        onItemViewClickedListener = ItemViewClickedListener(activity!!)
        setOnItemViewSelectedListener(ItemViewSelectedListener())

        super.onStart()
    }

    private fun discoverServices() {
        Log.i(TAG, "Looking for available devices")
        initDiscoveryListener()
        nsdManager!!.discoverServices(
            SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener
        )
    }

    private fun stopDiscovery() {
        if (discoveryListener != null) {
            try {
                nsdManager!!.stopServiceDiscovery(discoveryListener)
            } finally { }
        }
        discoveryListener = null
    }

    // On pause, the discovery listener will be stopped. Thus, the current devices inside
    // cardAdapter will be outdated by the time onResume() is called, and they will have to be
    // cleared out.
    override fun onPause() {
        Log.d(TAG, "Pausing.")
        stopDiscovery()
        cardAdapter.clear()
        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "Resuming.")
        super.onResume()
        discoverServices()
    }

    private class ItemViewClickedListener(private val activity: FragmentActivity) : OnItemViewClickedListener {

        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?, item: Any,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
            // If it's a device, a new activity is started to communicate with it and show
            // the videos.
            if (item is NsdServiceInfo) {
                Log.i(TAG, "Device clicked: $item");

                val intent = Intent(activity, VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.DEVICE_ARG, item)
                }
                startActivity(activity, intent, null)
            }
        }
    }

    // The ItemViewSelectedListener is empty, because nothing should happen in this case.
    private class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?, item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
        }
    }

    private fun initDiscoveryListener() {
        Log.i(TAG, "Initializing the discovery listener")
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "Service discovery success: $service")
                when {
                    service.serviceType != SERVICE_TYPE ->
                        Log.d(TAG, "Unknown Service Type: " + service.serviceType)
                    service.serviceName.contains(SERVICE_NAME) -> {
                        Log.d(TAG, "Resolving service: $SERVICE_NAME")
                        nsdManager!!.resolveService(service, CustomResolveListener())
                    }
                    else -> Log.d(TAG, "Name didn't match")
                }
            }

            // When a service is lost, every widget in the adapter has to be checked to remove it
            // from the GUI too.
            override fun onServiceLost(lost: NsdServiceInfo) {
                Log.e(TAG, "Service lost: $lost")

                for (i in 0 until cardAdapter.size()) {
                    if ((cardAdapter[i] as NsdServiceInfo).serviceName == lost.serviceName) {
                        Log.i(TAG, "Removed item from cardAdapter with index $i")
                        cardAdapter.removeItems(i, 1)
                        break
                    }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(
                serviceType: String,
                errorCode: Int
            ) {
                Log.e(TAG, "onStartDiscoveryFailed: $serviceType. Error code: $errorCode")
            }

            override fun onStopDiscoveryFailed(
                serviceType: String,
                errorCode: Int
            ) {
                Log.e(TAG, "onStopDiscoveryFailed: $serviceType. Error code: $errorCode")
            }
        }
    }

    private inner class CustomResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve succeeded: $serviceInfo")

            // The new device found is added as a card in the grid
            cardAdapter.add(serviceInfo)
        }
    }

}
