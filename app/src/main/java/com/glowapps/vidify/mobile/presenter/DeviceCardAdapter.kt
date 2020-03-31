package com.glowapps.vidify.mobile.presenter

import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glowapps.vidify.R
import com.glowapps.vidify.nsd.getDescription
import com.glowapps.vidify.nsd.getImage
import com.glowapps.vidify.nsd.getTitle
import kotlinx.android.synthetic.main.mobile_device_card.view.*


class DeviceCardAdapter : RecyclerView.Adapter<DeviceCardAdapter.CardViewHolder>() {
    companion object {
        private const val TAG = "DeviceCardAdapter"
    }

    val elements = mutableListOf<NsdServiceInfo>()
    private var clickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        Log.i(TAG, "Creating new ViewHolder")
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.mobile_device_card, parent, false) as CardView
        return CardViewHolder(cardView)
    }

    override fun getItemCount() = elements.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val view = holder.cardView!!
        val device = elements[position]
        Log.i(TAG, "Binding new device: ${device.serviceName}")

        view.card_title.text = getTitle(device)
        view.card_description.text = getDescription(device)
        Glide.with(view.context)
            .load(getImage(device))
            .apply(RequestOptions.errorOf(R.drawable.os_unknown))
            .into(view.card_image)
    }

    fun add(service: NsdServiceInfo) {
        Log.i(TAG, "Adding new service: $service")
        elements.add(service)
        notifyItemInserted(elements.size - 1)
    }

    fun remove(i: Int) {
        Log.i(TAG, "Removing service: $i")
        elements.removeAt(i)
        notifyItemRemoved(i)
    }

    fun clear() {
        Log.i(TAG, "Clearing all services")
        elements.clear()
        notifyDataSetChanged()
    }

    fun setClickListener(listener: ItemClickListener) {
        this.clickListener = listener
        notifyDataSetChanged()
    }

    inner class CardViewHolder(view: CardView) : RecyclerView.ViewHolder(view),
            View.OnClickListener {

        var cardView: CardView? = null

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
