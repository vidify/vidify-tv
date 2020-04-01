package com.glowapps.vidify.player

import android.net.nsd.NsdServiceInfo
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import com.glowapps.vidify.BuildConfig
import com.glowapps.vidify.model.Message
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException

private class ConfirmFailedException(message: String) : Exception(message)

// Takes two callbacks, to start a new video and to update it.
class VidifyConnection(
    private val service: NsdServiceInfo,
    private val startVideo: (Message) -> Unit,
    private val updateVideo: (Message) -> Unit
) : Runnable {
    companion object {
        const val TAG = "VidifyConnection"
    }

    private lateinit var socket: Socket
    private var curVideo: String? = null
    private var isPlaying: Boolean? = null

    override fun run() {
        try {
            connect()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Failed to connect to socket: $e")
            // TODO callback to send Toast within VideoPlayerActivity and go back to the
            //  previous activity (2).
            e.printStackTrace()
            return
        }

        val jsonInput = JsonReader(InputStreamReader(socket.getInputStream(), "utf-8"))
        jsonInput.isLenient = true  // More tolerable JSON parser
        val jsonOutput = JsonWriter(OutputStreamWriter(socket.getOutputStream(), "utf-8"))

        try {
            confirm(jsonInput, jsonOutput)

            while (true) {
                // Obtaining the message and parsing it. If the received URL is the same as the
                // one currently playing, the video is updated. Otherwise, a new one starts
                // playing with its properties.
                val msg = readMessage(jsonInput)
                Log.i(TAG, "MESSAGE READ: $msg")
                if (msg.url == curVideo) {
                    updateVideo(msg)
                } else {
                    startVideo(msg)
                    curVideo = msg.url
                }
                if (msg.isPlaying != null) {
                    isPlaying = msg.isPlaying!!
                }
            }
        } catch (e: SocketException) {
            // A disconnection was performed outside this thread.
            Log.i(TAG, "Stopping client thread")
            return
        } catch (e: Exception) {
            // Unexpected error
            // TODO callback to send Toast within VideoPlayerActivity and go back to the
            //  previous activity.
            e.printStackTrace()
            disconnect()
            return
        }
    }

    private fun connect() {
        Log.i(TAG, "Connecting to the service ${service.serviceName}")
        socket = Socket(service.host, service.port)
    }

    @Synchronized
    fun disconnect() {
        Log.i(TAG, "Disconnecting from the service ${service.serviceName}")
        socket.close()
    }

    private fun confirm(input: JsonReader, output: JsonWriter) {
        // Sending the app data
        Log.i(TAG, "Sending confirmation")
        output.beginObject()
        output.name("id").value(BuildConfig.APPLICATION_ID)
        output.name("version").value(BuildConfig.VERSION_NAME)
        output.endObject()
        output.flush()

        // Receiving the server confirmation
        Log.i(TAG, "Waiting for reply")
        input.beginObject()
        var success: Boolean? = null
        var msg: String? = null
        while (input.hasNext()) {
            when (val name = input.nextName()) {
                "success" -> success = input.nextBoolean()
                "error_msg" -> msg = input.nextString()
                else -> {
                    Log.e(TAG, "Unexpected parameter in JSON message: $name")
                    input.skipValue()
                }
            }
        }
        input.endObject()

        if (success == null || !success) {
            if (msg == null) {
                msg = "Unknown error"
            }
            throw ConfirmFailedException("The server refused to connect: $msg")
        }

        Log.i(TAG, "Confirm successful")
    }

    private fun readMessage(input: JsonReader): Message {
        Log.i(TAG, "Starting message parse")
        var url: String? = null
        var absolutePos: Int? = null
        var relativePos: Int? = null
        var isPlaying: Boolean? = null

        input.beginObject()
        while (input.hasNext()) {
            when (val name = input.nextName()) {
                "url" -> url = input.nextString()
                "absolute_pos" -> absolutePos = input.nextInt()
                "relative_pos" -> relativePos = input.nextInt()
                "is_playing" -> isPlaying = input.nextBoolean()
                else -> {
                    Log.e(TAG, "Unexpected parameter in JSON message: $name")
                    input.skipValue()
                }
            }
        }
        input.endObject()
        Log.i(TAG, "Finished parsing the message")

        return Message(url, absolutePos, relativePos, isPlaying)
    }
}