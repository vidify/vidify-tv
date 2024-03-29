package com.glowapps.vidify.tv.presenter

import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glowapps.vidify.R
import com.glowapps.vidify.nsd.getTitle
import com.glowapps.vidify.nsd.getDescription
import com.glowapps.vidify.nsd.getImage

// The CardPresenter generates card views, given their name and other basic attributes.
class DeviceCardPresenter : Presenter() {
    companion object {
        const val TAG = "DefaultCardPresenter"
    }

    private var selectedBackgroundColor = -1
    private var defaultBackgroundColor = -1

    // The parent ViewGroup contains various views that are going to be created within the
    // presenter.
    // The returned ViewHolder describes an item view and metadata about its place.
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        // The cards will have two colors: the default one, and the selected color. The latter
        // will be set when the user focuses the card, or selects it. They will also have an
        // image.
        defaultBackgroundColor =
            ContextCompat.getColor(
                parent!!.context,
                R.color.default_background
            )
        selectedBackgroundColor =
            ContextCompat.getColor(
                parent.context,
                R.color.selected_background
            )

        val cardView: ImageCardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    // Switching between the default and selected colors.
    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) selectedBackgroundColor else defaultBackgroundColor
        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color)
        view.findViewById<View>(R.id.info_field).setBackgroundColor(color)
    }

    // This is called when a view is recycled inside a RecyclerView.
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        // Setting the card's basic attributes
        val cardView = viewHolder.view as ImageCardView
        val width = cardView.resources.getDimensionPixelSize(R.dimen.default_card_width)
        val height = cardView.resources.getDimensionPixelSize(R.dimen.default_card_height)
        cardView.setMainImageDimensions(width, height)

        // Setting the card's contents: title, description and image
        val device: NsdServiceInfo = item as NsdServiceInfo
        Log.i(TAG, "refreshing card view for $device")
        cardView.titleText = getTitle(device)
        cardView.contentText = getDescription(device)
        val image = getImage(device)

        // Adding the image with Glide so that it will be cached.
        Glide.with(cardView.context)
            .load(image)
            .apply(RequestOptions.errorOf(R.drawable.os_unknown))
            .into(cardView.mainImageView)
    }

    // This only removes the view references so that the garbage collector can free up memory.
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder!!.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}
