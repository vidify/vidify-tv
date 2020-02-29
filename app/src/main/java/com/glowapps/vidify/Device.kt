package com.glowapps.vidify

// TODO: move this to a data/ directory. Same for the other files.

// This structure holds all the information about the available devices, which will be shown to
// the user in cards, and then a connection will be established.
data class Device(val name: String, val description: String, val cardImage: String,
                  val address: String, val port: Int)
