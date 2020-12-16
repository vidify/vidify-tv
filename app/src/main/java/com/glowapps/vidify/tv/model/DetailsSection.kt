package com.glowapps.vidify.tv.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// This package defines the different data structures needed to both create a card in the main
// fragment and start a DetailsSectionCardPresenter programmatically (only creating an Intent).

// The different types of sections.
enum class DetailsSectionCard {
    HELP,
    SUBSCRIBE,
    SHARE
}

// The type of buttons that can be used in DetailsSection (this can be expanded).
enum class DetailsSectionButtonAction(val id: Long) {
    SUBSCRIBE(0),
    SUBSCRIBE_DONE(1),
    SHARE(2)
}

// A parcelable alternative to Action with information about the button inside a DetailsSection
@Parcelize
data class DetailsSectionButton(
    var type: DetailsSectionButtonAction,
    var text: String
) : Parcelable

// Main dataclass with information to set-up both the section card and the fragment
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
