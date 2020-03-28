package com.glowapps.vidify.presenter

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.glowapps.vidify.R
import com.glowapps.vidify.model.DetailsSection


class DetailsSectionCardPresenter : Presenter() {
    companion object {
        private const val TAG = "MiscCardPresenter"
        private const val ANIMATION_DURATION = 200
    }

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        // Creating a new card view with the icon theme
        val wrapper = ContextThemeWrapper(parent!!.context, R.style.IconCardTheme)
        val imageCardView = ImageCardView(wrapper)
        imageCardView.isFocusable = true
        imageCardView.isFocusableInTouchMode = true
        imageCardView.mainImageView.setBackgroundResource(R.drawable.icon_focused)
        imageCardView.mainImageView.background.alpha = 0
        imageCardView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            animateIconBackground(
                imageCardView.mainImageView.background,
                hasFocus
            )
        }
        return ViewHolder(imageCardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val cardView = viewHolder.view as ImageCardView
        val card = item as DetailsSection
        cardView.titleText = card.title

        // Adding the image with Glide so that it will be cached.
        Glide.with(cardView.context)
            .load(getDrawable(cardView.context, card.card_image))
            .into(cardView.mainImageView)
    }

    // The opposite of onBindViewHolder. It only removes the view references so that the garbage
    // collector can free up memory.
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        val cardView = viewHolder!!.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    private fun animateIconBackground(drawable: Drawable, hasFocus: Boolean) {
        if (hasFocus) {
            ObjectAnimator.ofInt(drawable, "alpha", 0, 255)
                .setDuration(ANIMATION_DURATION.toLong())
                .start()
        } else {
            ObjectAnimator.ofInt(drawable, "alpha", 255, 0)
                .setDuration(ANIMATION_DURATION.toLong())
                .start()
        }
    }
}