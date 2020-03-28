package com.glowapps.vidify.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class DetailsSectionAction(val id: Int) {
    HELP(0),
    REMOVE_ADS(1),
    SHARE(2)
}

@Parcelize
data class DetailsSection(
    var card_title: String,
    var title: String,
    var subtitle: String,
    var description: String,
    var image: Int,
    var action: DetailsSectionAction
) : Parcelable
