package com.btb.beatthebox.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.btb.beatthebox.R
import com.bumptech.glide.Glide

class MediaPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_preview)

        val url = intent.getStringExtra("MEDIA_URL") ?: return
        val type = intent.getStringExtra("MEDIA_TYPE")

        val imageView = findViewById<ImageView>(R.id.previewImageView)

        if (type == "image") {
            imageView.visibility = View.VISIBLE

            Glide.with(this)
                .load(url)
                .into(imageView)
        }
    }

//    override fun onPause() {
//        super.onPause()
//        player?.pause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        player?.play()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        releasePlayer()
//    }
//
//    private fun releasePlayer() {
//        player?.release()
//        player = null
//    }
}
