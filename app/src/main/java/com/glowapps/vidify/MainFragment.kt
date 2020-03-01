package com.glowapps.vidify

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*
import com.glowapps.vidify.model.Device
import com.glowapps.vidify.presenter.CardPresenter


// TODO: Set fragment's description somewhere in the UI
// TODO: Custom message when there are no views with instructions on how to set Vidify up

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
    private var resolveListener: NsdManager.ResolveListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)

        // Including the presenter to manage the grid layout itself.
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)

        // Setting the card adapter, an interface used to manage the cards displayed in the
        // grid view. The cards are set up with a Device structure.
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
        initResolveListener()

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

    private class ItemViewClickedListener(activity: FragmentActivity) : OnItemViewClickedListener {
        private val mActivity: FragmentActivity = activity

        // Called when a user clicks on a item, which should be a Device structure.
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?, item: Any,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
            // If it's a device, a new activity is started to communicate with it and show
            // the videos.
            if (item is Device) {
                Log.i(TAG, "Connecting to device $item");

                val intent = Intent(mActivity, VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.DEVICE_ARG, item)
                }
                val bundle =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(mActivity)
                        .toBundle()
                startActivity(mActivity, intent, bundle)
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

    private fun initResolveListener() {
        Log.i(TAG, "Initializing the resolve listener")
        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "Resolve Succeeded: $serviceInfo")

                // The new device found is added as a card in the grid
                cardAdapter.add(
                    Device(
                        serviceInfo.serviceName,
                        serviceInfo.serviceType,
                        R.drawable.pic3,
                        serviceInfo
                    )
                )
            }
        }
    }

    private fun initDiscoveryListener() {
        Log.i(TAG, "Initializing the discovery listener")
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                // TODO: handle "java.lang.IllegalArgumentException: listener already in use"
                Log.d(TAG, "Service discovery success: $service")
                when {
                    service.serviceType != SERVICE_TYPE ->
                        Log.d(TAG, "Unknown Service Type: " + service.serviceType)
                    service.serviceName.contains(SERVICE_NAME) -> {
                        Log.d(TAG, "Resolving service: $SERVICE_NAME")
                        nsdManager!!.resolveService(service, resolveListener)
                    }
                    else -> Log.d(TAG, "Name didn't match")
                }
            }

            // When a service is lost, every widget in the adapter has to be checked to remove it
            // from the GUI too.
            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(TAG, "Service lost: $service")

                var device: Device
                for (i in 0 until cardAdapter.size()) {
                    device = cardAdapter[i] as Device
                    if (device.serviceInfo.serviceName == service.serviceName) {
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
}
