package com.glowapps.vidify.player

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.glowapps.vidify.R
import com.glowapps.vidify.billing.BillingSystem
import com.glowapps.vidify.model.Message
import com.glowapps.vidify.model.Purchasable
import com.glowapps.vidify.tv.BaseTVActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


class VideoPlayerActivity : BaseTVActivity() {
    companion object {
        const val TAG = "VideoPlayerActivity"
        const val DEVICE_ARG = "device_arg"
    }

    private lateinit var device: NsdServiceInfo

    private lateinit var youTubePlayerView: YouTubePlayerView
    private val youtubeIDRegex = Regex("""^.*(youtu\.be/|v/|u/\w/|embed/|watch\?v=|&v=)([^#&?]*).*""");

    private lateinit var billingSystem: BillingSystem

    private lateinit var connectionThread: Thread
    private lateinit var connectionRunnable: VidifyConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player_fragment)
        device = intent.getParcelableExtra(DEVICE_ARG)!!

        // The button has to be modified before its layout is inflated for the player
        toggleDemoMessage()
        initPlayer()
        startConnectionThread()
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroyed")
        connectionRunnable.disconnect()

        super.onDestroy()
    }

    override fun onResume() {
        Log.d(TAG, "Resuming fragment")
        // If the thread had enough time to die, it's restarted. Otherwise, the previous state
        // is restored (since the YouTube player is paused after onResume always).
        if (!connectionThread.isAlive) {
            startConnectionThread()
        }

        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "Pausing fragment")
        // Disconnecting with the Vidify server and stopping its thread.
        connectionRunnable.disconnect()

        super.onPause()
    }

    private fun startConnectionThread() {
        connectionRunnable = VidifyConnection(device, ::startVideo, ::updateVideo)
        connectionThread = Thread(connectionRunnable)
        connectionThread.start()
    }

    private fun initPlayer() {
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
        muteVideo(true)
    }

    private fun toggleDemoMessage() {
        billingSystem = BillingSystem(this)
        // If the full app is purchased the demo message will be removed
        if (billingSystem.isActive(Purchasable.SUBSCRIBE)) {
            val msg: TextView = findViewById(R.id.demo_message)
            msg.visibility = View.INVISIBLE
        }
    }

    // Start playing a new video from the message's url, and with its attributes
    @Synchronized
    fun startVideo(msg: Message) {
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                // A null url won't modify the player status. The player needs the YouTube ID,
                // so this first attempts to obtain it from the URL. If it fails, the status
                // won't be modified either.
                Log.i(TAG, "Obtaining ID from ${msg.url}")
                if (msg.url == null) return
                val id = getYouTubeID(msg.url!!) ?: return

                // When starting a new video, only the absolute position provided is taken
                // into account.
                val position = if (msg.absolutePos == null) {
                    0F
                } else {
                    msg.absolutePos!!.toFloat() / 1000F
                }

                Log.i(TAG, "Playing video with ID $id at $position seconds")
                youTubePlayer.loadVideo(id, position)

                // The video may also start paused.
                if (msg.isPlaying != null && !msg.isPlaying!!) {
                    youTubePlayer.pause()
                    Log.i(TAG, "The video starts paused")
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
                    // TODO this can probably be improved
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
    fun muteVideo(mute: Boolean) {
        youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                if (mute) {
                    youTubePlayer.mute()
                } else {
                    youTubePlayer.unMute()
                }
            }
        })
    }

    private fun getYouTubeID(url: String): String? {
        val match = youtubeIDRegex.find(url)
        if (match != null) {
            return match.groupValues[2]
        }

        return null
    }
}