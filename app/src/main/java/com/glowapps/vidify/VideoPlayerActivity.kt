package com.glowapps.vidify

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.glowapps.vidify.model.Message
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket


class VideoPlayerActivity : FragmentActivity() {
    companion object {
        const val TAG = "VideoPlayerActivity"
        const val DEVICE_ARG = "device"
    }

    private lateinit var device: NsdServiceInfo
    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var listenerThread: Thread

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting fullscreen and loading the activity layout to play the videos
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.video_player_activity)

        device = intent.getParcelableExtra(DEVICE_ARG)!!

        // Initializing the YouTube player and inserting it into the layout
        Log.i(TAG, "Creating YouTube player")
        youTubePlayerView = findViewById(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)
        // The custom player UI is as restricted and minimal as possible, since the user won't have
        // any control over what's playing.
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayerView.inflateCustomPlayerUi(R.layout.custom_youtube_player)
                youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {})
            }
        })
        youTubePlayerView.enterFullScreen()
        muteVideo()

        // Starting the thread that communicates with the server
        listenerThread = Thread(Listener())
        listenerThread.start()
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroyed")
        listenerThread.interrupt()

        super.onDestroy()
    }

    override fun onPause() {
        Log.d(TAG, "Paused")
        listenerThread.interrupt()

        super.onPause()
    }

    // Start playing a new video from the message's url, and with its attributes
    @Synchronized
    fun startVideo(msg: Message) {
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                val url = if (msg.url == null) {
                    // msg.url = R.drawable.default_video
                    "fx2Z5ZD_Rbo"  // nothing
                } else {
                    msg.url!!.split("watch?v=")[1]
                }
                val position = if (msg.absolutePos == null) {
                    0F
                } else {
                    msg.absolutePos!!.toFloat() / 1000F
                }
                Log.i(TAG, "Playing video with ID $url at $position seconds")
                youTubePlayer.loadVideo(url, position)
                if (msg.isPlaying != null && !msg.isPlaying!!) {
                    Log.i(TAG, "Video starts paused")
                    youTubePlayer.pause()
                }
            }
        })
    }

    // Update the currently playing video with the message's attributes
    @Synchronized
    fun updateVideo(msg: Message) {
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                // The absolute position has priority over the relative.
                if (msg.absolutePos != null) {
                    youTubePlayer.seekTo(msg.absolutePos!!.toFloat() / 1000F)
                } else if (msg.relativePos != null) {
                    val tracker = YouTubePlayerTracker()
                    youTubePlayer.addListener(tracker)
                    youTubePlayer.seekTo(tracker.currentSecond + msg.relativePos!!.toFloat() / 1000F)
                    youTubePlayer.removeListener(tracker)
                }
                if (msg.isPlaying != null) {
                    if (msg.isPlaying!!) {
                        youTubePlayer.play()
                    } else {
                        youTubePlayer.pause()
                    }
                }
            }
        })
    }

    @Synchronized
    fun muteVideo() {
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.mute()
            }
        })
    }

    @Synchronized
    fun unMuteVideo() {
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.unMute()
            }
        })
    }

    private inner class Listener : Runnable {
        private lateinit var socket: Socket
        private var curVideo: String? = null

        override fun run() {
            // First connecting to the device
            Log.i(TAG, "Connecting to the device in the Listener thread: $device")
            try {
                socket = Socket(device.host, device.port)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to connect to socket: $t")
                t.printStackTrace()
                return
            }

            val jsonInput = JsonReader(InputStreamReader(socket.getInputStream(), "utf-8"))
            jsonInput.isLenient = true  // More tolerable JSON parser
            while (!socket.isClosed && !Thread.currentThread().isInterrupted) {
                // Obtaining the message and parsing it. If the received URL is the same as the
                // one currently playing, the video is updated. Otherwise, a new one starts
                // playing with its properties.
                val msg = readMessage(jsonInput)
                if (msg.url == curVideo) {
                    updateVideo(msg)
                } else {
                    startVideo(msg)
                    curVideo = msg.url
                }
            }
            disconnect()
            Log.i(TAG, "Stopped listening for messages")
        }

        private fun readMessage(input: JsonReader): Message {
            Log.i(TAG, "Parsing a message")
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

            return Message(url, absolutePos, relativePos, isPlaying)
        }

        fun disconnect() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error when trying to close the socket")
                e.printStackTrace()
            }
        }
    }
}