package com.btb.beatthebox.ui.about

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class CarouselAdapter(
    private val mediaUrls: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_VIDEO = 1
    }

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        releaseAllPlayers()
        this.recyclerView = null
    }

    // --- Image ViewHolder ---
    inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    // --- Video ViewHolder ---
    inner class VideoViewHolder(val playerView: PlayerView) : RecyclerView.ViewHolder(playerView) {
        var player: ExoPlayer? = null
    }

    override fun getItemViewType(position: Int): Int {
        val url = mediaUrls[mediaUrls.size - 1 - position]
        return if (url.endsWith(".mp4") || url.endsWith(".mov") || url.endsWith(".mkv")) {
            TYPE_VIDEO
        } else {
            TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            ImageViewHolder(imageView)
        } else {
            val playerView = PlayerView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = true // show play/pause controls
            }
            VideoViewHolder(playerView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val url = mediaUrls[mediaUrls.size - 1 - position]

        when (holder) {
            is ImageViewHolder -> {
                Glide.with(holder.imageView.context)
                    .load(url)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imageView)

                holder.imageView.setOnClickListener {
                    onMediaClick?.invoke(url, TYPE_IMAGE)
                }
            }

            is VideoViewHolder -> {
                holder.player?.release() // cleanup old player

                val context = holder.playerView.context
                val player = ExoPlayer.Builder(context).build()
                holder.player = player
                holder.playerView.player = player

                val mediaItem = MediaItem.fromUri(url)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                player.volume = 0f
                holder.playerView.setOnClickListener {
                    onMediaClick?.invoke(url, TYPE_VIDEO)
                }
            }
        }
    }

    override fun getItemCount(): Int = mediaUrls.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is VideoViewHolder) {
            holder.player?.release()
            holder.player = null
        }
    }

    var onMediaClick: ((url: String, type: Int) -> Unit)? = null

    fun releaseAllPlayers() {
        for (i in 0 until itemCount) {
            val holder = recyclerView?.findViewHolderForAdapterPosition(i)
            if (holder is VideoViewHolder) {
                holder.player?.release()
                holder.player = null
            }
        }
    }

    fun releaseAllExcept(position: Int) {
        for (i in 0 until itemCount) {
            if (i == position) continue
            val holder = recyclerView?.findViewHolderForAdapterPosition(i)
            if (holder is VideoViewHolder) {
                holder.player?.pause()
            }
        }
    }

    fun pauseAll() {
        for (i in 0 until itemCount) {
            val holder = recyclerView?.findViewHolderForAdapterPosition(i)
            if (holder is VideoViewHolder) {
                holder.player?.pause()
            }
        }
    }

    fun pause(position: Int) {
        val holder = recyclerView?.findViewHolderForAdapterPosition(position)
        if (holder is VideoViewHolder) {
            holder.player?.pause()
        }
    }
}
