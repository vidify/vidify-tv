package com.glowapps.vidify

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.glowapps.vidify.model.MiscAction
import com.glowapps.vidify.model.MiscCard
import com.glowapps.vidify.nsd.DeviceDiscoveryListener
import com.glowapps.vidify.presenter.DeviceCardPresenter
import com.glowapps.vidify.presenter.MiscCardPresenter


class MainFragment : BrowseSupportFragment() {
    companion object {
        const val TAG = "MainFragment"
        const val SERVICE_TYPE = "_vidify._tcp."
        const val SERVICE_NAME = "vidify"
        const val SERVICE_PROTOCOL = NsdManager.PROTOCOL_DNS_SD  // DNS-based service discovery
    }

    // Bigger adapter to hold all rows
    private lateinit var rowsAdapter: ArrayObjectAdapter
    // Row adapter for the devices in the network
    private lateinit var deviceAdapter: ArrayObjectAdapter
    // Row with other cards, like settings, disabling ads, help...
    private lateinit var miscAdapter: ArrayObjectAdapter
    private var nsdManager: NsdManager? = null
    private var discoveryListener: DeviceDiscoveryListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)

        // The column to the left is enabled, and pressing back will return to it.
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Initializing the callbacks for the items
        onItemViewClickedListener = ItemViewClickedListener(activity!!)
        onItemViewSelectedListener = ItemViewSelectedListener()

        // Loading the grid state, trying to reuse the previous one
        if (savedInstanceState == null) {
            prepareEntranceTransition()
        }

        // Setting the card adapter, an interface used to manage the cards displayed in the
        // grid view.
        Handler().postDelayed({
            createRows()
            startEntranceTransition()
        }, 500)
    }

    private fun createRows() {
        // The bigger adapter for the header + cards
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter().apply {
            // By default, no shadows for the rows (needed for the misc cards)
            shadowEnabled = false
        })
        adapter = rowsAdapter

        // The first row contains the devices in the network, with a header named "Devices".
        deviceAdapter = ArrayObjectAdapter(DeviceCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem(0, "Devices"), deviceAdapter))

        // The second row contains other cards for settings and such
        miscAdapter = ArrayObjectAdapter(MiscCardPresenter())
        miscAdapter.add(MiscCard("Help", R.drawable.help_icon, MiscAction.HELP))
        miscAdapter.add(MiscCard("Remove Ads", R.drawable.remove_ads_icon, MiscAction.REMOVE_ADS))
        miscAdapter.add(MiscCard("Share", R.drawable.share_icon, MiscAction.SHARE))
        miscAdapter.add(MiscCard("Website", R.drawable.website_icon, MiscAction.WEBSITE))
        rowsAdapter.add(ListRow(HeaderItem(0, "More"), miscAdapter))
    }

    override fun onStart() {
        // Initializing the network service discovery
        nsdManager = activity!!.getSystemService(Context.NSD_SERVICE) as NsdManager

        super.onStart()
    }

    private fun startDiscovery() {
        Log.i(TAG, "Looking for available devices")
        discoveryListener = DeviceDiscoveryListener(nsdManager!!, ::addService, ::removeService)
        nsdManager!!.discoverServices(
            SERVICE_TYPE, SERVICE_PROTOCOL, discoveryListener
        )
    }

    private fun stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager!!.stopServiceDiscovery(discoveryListener)
        }
        discoveryListener = null
    }

    // On pause, the discovery listener will be stopped. Thus, the current devices inside
    // deviceAdapter will be outdated by the time onResume() is called, and they will have to be
    // cleared out.
    override fun onPause() {
        Log.d(TAG, "Pausing.")
        stopDiscovery()
        deviceAdapter.clear()
        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "Resuming.")
        super.onResume()
        startDiscovery()
    }

    private class ItemViewClickedListener(private val activity: FragmentActivity) :
        OnItemViewClickedListener {

        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?, item: Any,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
            if (item is NsdServiceInfo) {
                // If it's a device, a new activity is started to communicate with it and show
                // the videos.
                Log.i(TAG, "Device clicked: $item");

                val intent = Intent(activity, VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.DEVICE_ARG, item)
                }
                startActivity(activity, intent, null)
            } else if (item is MiscCard) {
                Log.i(TAG, "Miscellaneous card clicked: $item")
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

    private fun addService(service: NsdServiceInfo) {
        // The new device found is added as a card in the grid. The UI can
        // only be modified within the main thread.
        Handler(Looper.getMainLooper()).post {
            // code goes here
            deviceAdapter.add(service)
        }
    }

    // When a service is lost, every widget in the adapter has to be checked to remove it
    // from the GUI too.
    private fun removeService(service: NsdServiceInfo) {
        for (i in 0 until deviceAdapter.size()) {
            if ((deviceAdapter[i] as NsdServiceInfo).serviceName == service.serviceName) {
                Log.i(MainFragment.TAG, "Removed item from deviceAdapter with index $i")
                deviceAdapter.removeItems(i, 1)
                break
            }
        }
    }
}
