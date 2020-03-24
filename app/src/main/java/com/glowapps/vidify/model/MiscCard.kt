package com.glowapps.vidify.model

enum class MiscAction(val id: Int) {
    HELP(0),
    REMOVE_ADS(1),
    WEBSITE(2),
    SHARE(3)
}

data class MiscCard(
    var title: String,
    var image: Int,
    var action: MiscAction
)
