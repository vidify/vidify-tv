package com.glowapps.vidify.mobile.presenter

import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glowapps.vidify.R
import com.glowapps.vidify.databinding.MobileDeviceCardBinding
import com.glowapps.vidify.nsd.getDescription
import com.glowapps.vidify.nsd.getImage
import com.glowapps.vidify.nsd.getTitle

// Requires two callbacks for whenever the view is set to empty or not.
class DeviceCardAdapter(
    private val viewEmpty: () -> Unit,
    private val viewNotEmpty: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TAG = "DeviceCardAdapter"
    }

    val devices = mutableListOf<NsdServiceInfo>()
    private var clickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        Log.i(TAG, "Creating new ViewHolder")
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.mobile_device_card, parent, false) as CardView
        return CardViewHolder(cardView)
    }

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with (holder as CardViewHolder) {
            val device = devices[position]
            Log.i(TAG, "Binding new device: ${device.serviceName}")

            binding.cardTitle.text = getTitle(device)
            binding.cardDescription.text = getDescription(device)
            Glide.with(cardView!!.context)
                .load(getImage(device))
                .apply(RequestOptions.errorOf(R.drawable.os_unknown))
                .into(binding.cardImage)
        }
    }

    fun add(service: NsdServiceInfo) {
        Log.i(TAG, "Adding new service: ${service.serviceName}")
        // If the first device is added, the empty view will be hidden
        if (devices.size == 0) {
            viewNotEmpty()
        }
        devices.add(service)
        notifyDataSetChanged()
    }

    fun remove(i: Int) {
        Log.i(TAG, "Removing service: $i")
        devices.removeAt(i)
        if (devices.size == 0) {
            viewEmpty()
        }
        notifyDataSetChanged()
    }

    fun clear() {
        Log.i(TAG, "Clearing all services")
        devices.clear()
        viewEmpty()
        notifyDataSetChanged()
    }

    fun setClickListener(listener: ItemClickListener) {
        this.clickListener = listener
    }

    inner class CardViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        var cardView: CardView? = null
        val binding = MobileDeviceCardBinding.bind(view)

        init {
            cardView = view.findViewById(R.id.card_view)
            view.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (clickListener != null){
                clickListener!!.onItemClick(view, adapterPosition)
            }
        }
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}
