package com.btb.beatthebox.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.btb.beatthebox.R
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class MediaPreviewActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_preview)

        val url = intent.getStringExtra("MEDIA_URL") ?: return
        val type = intent.getStringExtra("MEDIA_TYPE")

        val imageView = findViewById<ImageView>(R.id.previewImageView)
        playerView = findViewById(R.id.previewPlayerView)

        if (type == "image") {
            imageView.visibility = View.VISIBLE
            playerView.visibility = View.GONE

            Glide.with(this)
                .load(url)
                .into(imageView)
        } else {
            imageView.visibility = View.GONE
            playerView.visibility = View.VISIBLE

            player = ExoPlayer.Builder(this).build().apply {
                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
            playerView.player = player
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
