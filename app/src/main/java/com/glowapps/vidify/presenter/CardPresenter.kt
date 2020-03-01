package com.glowapps.vidify.presenter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glowapps.vidify.R
import com.glowapps.vidify.model.Device


// The CardPresenter generates card views, given their name and other basic attributes.
class CardPresenter : Presenter() {
    private var mSelectedBackgroundColor = -1
    private var mDefaultBackgroundColor = -1
    private lateinit var mDefaultCardImage: Drawable

    // The parent ViewGroup contains various views that are going to be created within the
    // presenter.
    // The returned ViewHolder describes an item view and metadata about its place.
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        // The cards will have two colors: the default one, and the selected color. The latter
        // will be set when the user focuses the card, or selects it. They will also have an
        // image.
        mDefaultBackgroundColor =
            ContextCompat.getColor(parent!!.context,
                R.color.default_background
            )
        mSelectedBackgroundColor =
            ContextCompat.getColor(parent.context,
                R.color.selected_background
            )
        mDefaultCardImage = parent.resources.getDrawable(R.drawable.default_card_image, null)

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
        val color = if (selected) mSelectedBackgroundColor else mDefaultBackgroundColor
        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color)
        view.findViewById<View>(R.id.info_field).setBackgroundColor(color)
    }

    // This is called when a view is recycled inside a RecyclerView. Its attributes are updated
    // to the new Device structure.
    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        // Obtaining the parameters
        val device: Device = item as Device
        val cardView = viewHolder!!.view as ImageCardView

        // Setting the card's basic attributes
        val res = cardView.resources
        val width = res.getDimensionPixelSize(R.dimen.card_width)
        val height = res.getDimensionPixelSize(R.dimen.card_height)
        cardView.setMainImageDimensions(width, height)
        // Its contents: the title and a description
        cardView.titleText = device.name
        cardView.contentText = device.description
        // Adding the image with Glide so that it will be cached.
        Glide.with(cardView.context)
            .load(device.cardImage)
            .apply(RequestOptions.errorOf(mDefaultCardImage))
            .into(cardView.mainImageView)
    }

    // The opposite of onBindViewHolder. It only removes the view references so that the garbage
    // collector can free up memory.
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder!!.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}