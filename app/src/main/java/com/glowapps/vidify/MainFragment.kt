package com.glowapps.vidify

import android.app.UiModeManager
import android.content.Context
import android.content.Context.UI_MODE_SERVICE
import android.content.Intent
import android.content.res.Configuration
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
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.model.DetailsSectionButton
import com.glowapps.vidify.model.DetailsSectionButtonAction
import com.glowapps.vidify.model.DetailsSectionCard
import com.glowapps.vidify.nsd.DeviceDiscoveryListener
import com.glowapps.vidify.presenter.DetailsSectionCardPresenter
import com.glowapps.vidify.presenter.DeviceCardPresenter

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
        rowsAdapter.add(ListRow(HeaderItem(0, getString(R.string.devices_header)), deviceAdapter))

        // The second row contains other cards for settings and such
        miscAdapter = ArrayObjectAdapter(DetailsSectionCardPresenter())
        miscAdapter.add(
            DetailsSection(
                DetailsSectionCard.HELP,
                getString(R.string.section_help_title),
                getString(R.string.section_help_subtitle),
                getString(R.string.section_help_description),
                R.drawable.section_help_card,
                R.drawable.qrcode_github,
                null
            )
        )
        miscAdapter.add(
            DetailsSection(
                DetailsSectionCard.REMOVE_ADS,
                getString(R.string.section_remove_ads_title),
                getString(R.string.section_remove_ads_subtitle),
                getString(R.string.section_remove_ads_description),
                R.drawable.section_remove_ads_card,
                R.drawable.section_remove_ads_card,
                arrayListOf(
                    DetailsSectionButton(
                        DetailsSectionButtonAction.REMOVE_ADS,
                        getString(R.string.section_remove_ads_card_title)
                    )
                )
            )
        )
        miscAdapter.add(
            DetailsSection(
                DetailsSectionCard.SHARE,
                getString(R.string.section_share_title),
                getString(R.string.section_share_subtitle),
                getString(R.string.section_share_description),
                R.drawable.section_share_card,
                R.drawable.qrcode_playstore,
                null
            )
        )
        rowsAdapter.add(ListRow(HeaderItem(0, getString(R.string.more_header)), miscAdapter))
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
            } else if (item is DetailsSection) {
                Log.i(TAG, "Section card clicked: $item")

                val uiModeManager = activity.getSystemService(UI_MODE_SERVICE) as UiModeManager
                // Performing the action depending on the card data
                val intent: Intent =
                    if (item.type == DetailsSectionCard.SHARE
                        && uiModeManager.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION
                    ) {
                        // Sharing on a television will open an activity with a QR code and more
                        // details. On Android, the standard share menu will be shown.
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                activity.getString(R.string.app_name)
                            )
                            putExtra(
                                Intent.EXTRA_TEXT,
                                activity.getString(R.string.playstore_link)
                            )
                            type = "text/plain"
                        }
                    } else {
                        // The rest of the actions will use a DetailViewActivity
                        Intent(activity, DetailsSectionActivity::class.java).apply {
                            putExtra(DetailsSectionActivity.DATA_INTENT_ARG, item)
                        }
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
