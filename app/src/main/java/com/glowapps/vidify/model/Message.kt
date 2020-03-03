package com.glowapps.vidify.model

data class Message(
    var url: String?,
    var relativePos: Int?,
    var absolutePos: Int?,
    var isPlaying: Boolean?
)
