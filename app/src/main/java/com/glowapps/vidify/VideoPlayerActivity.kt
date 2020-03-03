package com.glowapps.vidify

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.glowapps.vidify.model.Device
import com.glowapps.vidify.model.Message
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket


// TODO: onPause, onResume and onDestroy

class VideoPlayerActivity : FragmentActivity() {
    companion object {
        const val TAG = "VideoPlayerActivity"
        const val DEVICE_ARG = "device"
    }

    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var device: Device
    private lateinit var socket: Socket
    private var curVideo: String? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting fullscreen and loading the activity layout to play the videos
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.video_player_activity)

        // First connecting to the device
        Thread {
            device = intent.getParcelableExtra(DEVICE_ARG)!!
            Log.i(TAG, "Connecting to the device in a new thread: ${device.serviceInfo}")
            try {
                socket = Socket(device.serviceInfo.host, device.serviceInfo.port)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to connect to socket: $t")
                t.printStackTrace()
                return@Thread
            }

            val jsonInput = JsonReader(InputStreamReader(socket.getInputStream(), "utf-8"))
            while (!socket.isClosed) {
                val msg = readMessage(jsonInput)
                if (msg.url != curVideo) {
                    // Start new video
                    startVideo(msg)
                    curVideo = msg.url
                } else {
                    // Update current video
                }
                Log.i(TAG, "READ: $msg")
            }
            Log.i(TAG,"Stop receiving messages, socket is closed")
        }.start()

        // Initializing the YouTube player and inserting it into the layout
        Log.i(TAG, "Creating YouTube player")
        val mainLayout = findViewById<LinearLayout>(R.id.youtube_layout)
        // Configuring the player
        youTubePlayerView = YouTubePlayerView(this)
        youTubePlayerView.enableAutomaticInitialization = false
        mainLayout.addView(youTubePlayerView)
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.enterFullScreen()
        youTubePlayerView.getPlayerUiController()
            .showBufferingProgress(false)
            .showCurrentTime(false)
            .showDuration(false)
            .showMenuButton(false)
            .enableLiveVideoUi(false)
            .showSeekBar(false)
            .showPlayPauseButton(false)
            .showVideoTitle(false)
            .showYouTubeButton(false)
            .showFullscreenButton(false)
        youTubePlayerView.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.mute()
            }
        })
    }

    fun readMessage(input: JsonReader): Message {
        var url: String? = null
        var absolutePos: Int? = null
        var relativePos: Int? = null
        var isPlaying: Boolean? = null

        input.beginObject()
        while (input.hasNext()) {
            when (val name = input.nextName()) {
                "url" -> url = input.nextString()
                "absolute_position" -> absolutePos = input.nextInt()
                "relative_position" -> relativePos = input.nextInt()
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

    fun startVideo(msg: Message) {
        youTubePlayerView.getYouTubePlayerWhenReady(object: YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                var url: String
                var position: Float
                if (msg.url == null) {
                    // msg.url = R.drawable.default_video
                    url = "fx2Z5ZD_Rbo"  // nothing
                } else {
                    url = msg.url!!.split("watch?v=")[1]
                    Log.d(TAG, "${msg.url!!.split("watch?v=")}")
                }
                if (msg.absolutePos == null) {
                    position = 0F
                } else {
                    position = msg.absolutePos!! / 1000F
                }
                youTubePlayer.loadVideo(url, position)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        // TODO: Synchronization with the connection thread, otherwise
        //  a `java.net.SocketException: Socket closed` is raised
        /*
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error when trying to close the socket")
            e.printStackTrace()
        }
         */
    }
}