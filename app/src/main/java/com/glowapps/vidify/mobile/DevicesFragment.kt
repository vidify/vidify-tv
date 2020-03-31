package com.glowapps.vidify.mobile

import android.content.Intent
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glowapps.vidify.R
import com.glowapps.vidify.mobile.presenter.DeviceCardAdapter
import com.glowapps.vidify.nsd.DeviceDiscoverySystem
import com.glowapps.vidify.player.VideoPlayerActivity
import com.glowapps.vidify.tv.MainTVFragment


class DevicesFragment : Fragment(), DeviceCardAdapter.ItemClickListener {
    companion object {
        private const val TAG = "DevicesFragment"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var viewAdapter: DeviceCardAdapter

    private var discoverySystem: DeviceDiscoverySystem? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.mobile_fragment_devices, container, false)
        emptyView = root.findViewById(R.id.empty_view) as LinearLayout

        viewAdapter = DeviceCardAdapter(::hideEmptyView, ::showEmptyView)
        viewAdapter.setClickListener(this)

        recyclerView = root.findViewById<RecyclerView>(R.id.devices_recycler_view).apply {
            layoutManager = LinearLayoutManager(activity!!)
            adapter = viewAdapter
        }

        return root
    }

    override fun onStart() {
        // Initializing the network service discovery
        discoverySystem = DeviceDiscoverySystem(activity!!, ::addService, ::removeService)

        super.onStart()
    }

    override fun onPause() {
        Log.d(MainTVFragment.TAG, "Pausing fragment")
        discoverySystem!!.stop()
        viewAdapter.clear()

        super.onPause()
    }

    override fun onResume() {
        Log.d(MainTVFragment.TAG, "Resuming fragment")
        super.onResume()

        discoverySystem!!.start()
    }

    private fun hideEmptyView() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }
    private fun showEmptyView() {
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    // When one of the cards is clicked, its video activity starts.
    override fun onItemClick(view: View?, position: Int) {
        val device = viewAdapter.devices[position]
        Log.i(TAG, "Item clicked: '${device.serviceName}', number $position")

        val intent = Intent(activity, VideoPlayerActivity::class.java).apply {
            putExtra(VideoPlayerActivity.DEVICE_ARG, device)
        }
        ContextCompat.startActivity(activity!!, intent, null)
    }

    private fun addService(service: NsdServiceInfo) {
        Handler(Looper.getMainLooper()).post {
            viewAdapter.add(service)
        }
    }

    // When a service is lost, every widget in the adapter has to be checked to remove it
    // from the GUI too.
    private fun removeService(service: NsdServiceInfo) {
        Handler(Looper.getMainLooper()).post {
            for (i in viewAdapter.devices.indices) {
                if ((viewAdapter.devices[i]).serviceName == service.serviceName) {
                    Log.i(MainTVFragment.TAG, "Removed item from deviceAdapter with index $i")
                    viewAdapter.remove(i)
                    break
                }
            }
        }
    }
}
