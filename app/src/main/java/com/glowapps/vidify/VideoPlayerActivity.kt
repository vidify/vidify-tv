package com.glowapps.vidify

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.glowapps.vidify.model.Device
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket


class VideoPlayerActivity : FragmentActivity() {
    companion object {
        const val TAG = "VideoPlayerActivity"
        const val DEVICE_ARG = "device"
    }

    private lateinit var youTubePlayerView: YouTubePlayerView

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
            val device = intent.getParcelableExtra<Device>(DEVICE_ARG)!!
            Log.i(TAG, "Connecting to the device in a new thread: ${device.serviceInfo}")
            val socket: Socket
            try {
                socket = Socket(device.serviceInfo.host, device.serviceInfo.port)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to connect to socket: $t")
                t.printStackTrace()
                return@Thread
            }
            val sockInput = BufferedReader(InputStreamReader(socket.getInputStream()))
            var json: JSONObject
            while (!socket.isClosed) {
                json = JSONObject(sockInput.readLine())
                Log.i(TAG, "Received $json")
            }
        }.start()

        // Initializing the YouTube player and inserting it into the layout
        Log.i(TAG, "Creating YouTube player")
        val mainLayout = findViewById<LinearLayout>(R.id.youtube_layout)
        youTubePlayerView = YouTubePlayerView(this)
        mainLayout.addView(youTubePlayerView)
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.enterFullScreen()
        youTubePlayerView.addYouTubePlayerListener(youTubePlayerListener)
    }

    private val youTubePlayerListener =  object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            youTubePlayer.mute()
            val videoId = "dQw4w9WgXcQ"
            youTubePlayer.loadVideo(videoId, 0f)
        }
    }
}