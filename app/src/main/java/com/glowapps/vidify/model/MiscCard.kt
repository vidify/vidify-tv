package com.glowapps.vidify.model

enum class MiscAction(val id: Int) {
    HELP(0),
    REMOVE_ADS(1),
    SHARE(2)
}

data class MiscCard(
    var title: String,
    var image: Int,
    var action: MiscAction
)
