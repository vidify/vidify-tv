package com.glowapps.vidify.model

import android.os.Parcelable
import androidx.leanback.widget.Action
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

// The different types of DetailsSection
enum class DetailsSectionCard {
    HELP,
    REMOVE_ADS,
    SHARE
}

// The buttons used in the sections
enum class DetailsSectionButtonAction(val id: Long) {
    REMOVE_ADS(0)
}

// A parcelable alternative to Action with information about the button
@Parcelize
data class DetailsSectionButton(
    var type: DetailsSectionButtonAction,
    var text: String
) : Parcelable

// Information about the section button and the fragment data itself
@Parcelize
data class DetailsSection(
    var type: DetailsSectionCard,
    var title: String,
    var subtitle: String,
    var description: String,
    var card_image: Int,
    var image: Int,
    var actions: ArrayList<DetailsSectionButton>?
) : Parcelable
