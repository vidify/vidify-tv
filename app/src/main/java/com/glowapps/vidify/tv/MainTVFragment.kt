package com.glowapps.vidify.tv

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
import com.glowapps.vidify.R
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.model.DetailsSectionButton
import com.glowapps.vidify.model.DetailsSectionButtonAction
import com.glowapps.vidify.model.DetailsSectionCard
import com.glowapps.vidify.nsd.DeviceDiscoverySystem
import com.glowapps.vidify.player.VideoPlayerActivity
import com.glowapps.vidify.tv.presenter.SectionCardPresenter
import com.glowapps.vidify.tv.presenter.DeviceCardPresenter

class MainTVFragment : BrowseSupportFragment() {
    companion object {
        const val TAG = "MainTVFragment"
    }

    // Bigger adapter to hold all rows
    private lateinit var rowsAdapter: ArrayObjectAdapter
    // Row adapter for the devices in the network
    private lateinit var deviceAdapter: ArrayObjectAdapter
    // Row with other cards, like settings, disabling ads, help...
    private lateinit var sectionAdapter: ArrayObjectAdapter

    private var discoverySystem: DeviceDiscoverySystem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)

        // The column to the left is enabled, and pressing back will return to it.
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Initializing the callbacks for the items
        onItemViewClickedListener =
            ItemViewClickedListener(
                activity!!
            )

        // Loading the grid state, trying to reuse the previous one
        if (savedInstanceState == null) {
            prepareEntranceTransition()
        }

        initAdapters()

        // Setting the card adapter, an interface used to manage the cards displayed in the
        // grid view.
        Handler().postDelayed({
            createRows()
            startEntranceTransition()
        }, 500)
    }

    private fun initAdapters() {
        // The bigger adapter for the header + cards
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter().apply {
            // By default, no shadows for the rows (needed for the misc cards)
            shadowEnabled = false
        })
        adapter = rowsAdapter

        // The first row contains the devices in the network, with a header named "Devices".
        deviceAdapter = ArrayObjectAdapter(DeviceCardPresenter())
        // The second row contains other cards for settings and such
        sectionAdapter = ArrayObjectAdapter(SectionCardPresenter())
    }

    private fun createRows() {
        rowsAdapter.add(ListRow(HeaderItem(0, getString(R.string.devices_header)), deviceAdapter))

        sectionAdapter.add(
            DetailsSection(
                DetailsSectionCard.HELP,
                getString(R.string.section_help_title),
                getString(R.string.section_help_subtitle),
                getString(R.string.section_help_description),
                R.drawable.icon_help,
                R.drawable.qrcode_github,
                null
            )
        )
        sectionAdapter.add(
            DetailsSection(
                DetailsSectionCard.SUBSCRIBE,
                getString(R.string.section_subscribe_title),
                getString(R.string.section_subscribe_subtitle),
                getString(R.string.section_subscribe_description),
                R.drawable.icon_subscribe,
                R.drawable.icon_subscribe,
                arrayListOf(
                    DetailsSectionButton(
                        DetailsSectionButtonAction.SUBSCRIBE,
                        getString(R.string.section_subscribe_button)
                    )
                )
            )
        )
        sectionAdapter.add(
            DetailsSection(
                DetailsSectionCard.SHARE,
                getString(R.string.section_share_title),
                getString(R.string.section_share_subtitle),
                getString(R.string.section_share_description),
                R.drawable.icon_share,
                R.drawable.qrcode_playstore,
                null
            )
        )
        rowsAdapter.add(ListRow(HeaderItem(0, getString(R.string.more_header)), sectionAdapter))
    }

    override fun onStart() {
        // Initializing the network service discovery
        discoverySystem = DeviceDiscoverySystem(activity!!, ::addService, ::removeService)

        super.onStart()
    }

    // On pause, the discovery listener will be stopped. Thus, the current devices inside
    // deviceAdapter will be outdated by the time onResume() is called, and they will have to be
    // cleared out.
    override fun onPause() {
        Log.d(TAG, "Pausing fragment")
        discoverySystem!!.stop()
        deviceAdapter.clear()

        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "Resuming fragment")
        super.onResume()

        discoverySystem!!.start()
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

                // Performing the action depending on the card data
                val intent = Intent(activity, DetailsSectionActivity::class.java).apply {
                    putExtra(DetailsSectionActivity.DATA_INTENT_ARG, item)
                }
                startActivity(activity, intent, null)
            }
        }
    }

    private fun addService(service: NsdServiceInfo) {
        // The new device found is added as a card in the grid. The UI can only be modified within
        // the main thread.
        Handler(Looper.getMainLooper()).post {
            deviceAdapter.add(service)
        }
    }

    // When a service is lost, every widget in the adapter has to be checked to remove it
    // from the GUI too.
    private fun removeService(service: NsdServiceInfo) {
        Handler(Looper.getMainLooper()).post {
            for (i in 0 until deviceAdapter.size()) {
                if ((deviceAdapter[i] as NsdServiceInfo).serviceName == service.serviceName) {
                    Log.i(TAG, "Removed item from deviceAdapter with index $i")
                    deviceAdapter.removeItems(i, 1)
                    break
                }
            }
        }
    }
}
