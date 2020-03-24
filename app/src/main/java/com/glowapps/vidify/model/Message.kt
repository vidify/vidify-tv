package com.glowapps.vidify.model

// Dataclass used to serialize the messages sent and received between devices.
data class Message(
    var url: String?,
    var relativePos: Int?,
    var absolutePos: Int?,
    var isPlaying: Boolean?
)
