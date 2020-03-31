package com.glowapps.vidify.mobile

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glowapps.vidify.R
import com.glowapps.vidify.mobile.presenter.DeviceCardAdapter
import com.glowapps.vidify.nsd.DeviceDiscoverySystem
import com.glowapps.vidify.tv.MainTVFragment


class DevicesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var discoverySystem: DeviceDiscoverySystem? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.mobile_fragment_devices, container, false)
        emptyView = root.findViewById(R.id.empty_view) as LinearLayout

        viewManager = LinearLayoutManager(activity!!)
        viewAdapter = DeviceCardAdapter()

        recyclerView = root.findViewById<RecyclerView>(R.id.devices_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
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
        // deviceAdapter.clear()

        super.onPause()
    }

    override fun onResume() {
        Log.d(MainTVFragment.TAG, "Resuming fragment")
        super.onResume()

        discoverySystem!!.start()
    }

    // Toggle the empty view visibility. It starts visible.
    private fun toggleEmptyView() {
        val previous = recyclerView.visibility
        recyclerView.visibility = emptyView.visibility
        emptyView.visibility = previous
    }

    private fun addService(service: NsdServiceInfo) {
        Handler(Looper.getMainLooper()).post {
            // deviceAdapter.add(service)
        }
    }

    // When a service is lost, every widget in the adapter has to be checked to remove it
    // from the GUI too.
    private fun removeService(service: NsdServiceInfo) {
        Handler(Looper.getMainLooper()).post {
            // for (i in 0 until deviceAdapter.size()) {
            //     if ((deviceAdapter[i] as NsdServiceInfo).serviceName == service.serviceName) {
            //         Log.i(MainTVFragment.TAG, "Removed item from deviceAdapter with index $i")
            //         deviceAdapter.removeItems(i, 1)
            //         break
            //     }
            // }
        }
    }
}
