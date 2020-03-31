package com.glowapps.vidify.mobile.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.glowapps.vidify.R

class DeviceCardAdapter : RecyclerView.Adapter<DeviceCardAdapter.CardViewHolder>() {
    class CardViewHolder(private val view: CardView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.mobile_device_card, parent, false) as CardView
        return CardViewHolder(cardView)
    }

    override fun getItemCount() = 0

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
    }
}
