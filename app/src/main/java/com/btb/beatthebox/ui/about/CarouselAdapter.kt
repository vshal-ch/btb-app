package com.btb.beatthebox.ui.about

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class CarouselAdapter(
    private val mediaUrls: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_VIDEO = 1
    }

    private var recyclerView: RecyclerView? = null

    private val youtubeViewCache = mutableMapOf<Int, YouTubePlayerView>()


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
    inner class YoutubeViewHolder(val container: FrameLayout) :
        RecyclerView.ViewHolder(container)

    override fun getItemViewType(position: Int): Int {
        val url = mediaUrls[mediaUrls.size - 1 - position]
        return if (isYoutubeUrl(url)) TYPE_VIDEO else TYPE_IMAGE
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
            val frame = FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = ViewGroup.generateViewId()
            }
            YoutubeViewHolder(frame)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val actualIndex = mediaUrls.size - 1 - position
        val url = mediaUrls[actualIndex]

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

            is YoutubeViewHolder -> {
                val container = holder.container
                container.removeAllViews()

                val cachedView = youtubeViewCache[actualIndex]


                if (cachedView != null) {
                    // Re-attach existing player
                    val parent = cachedView.parent as? ViewGroup
                    parent?.removeView(cachedView)
                    container.addView(cachedView)
                } else {
                    // Create a new YouTubePlayerView
                    val newYoutubeView = YouTubePlayerView(container.context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val lifecycleOwner = recyclerView?.context as? LifecycleOwner
                    lifecycleOwner?.lifecycle?.addObserver(newYoutubeView)

                    val videoId = extractYoutubeVideoId(url)
                    newYoutubeView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.cueVideo(videoId, 0f)
                        }
                    })

                    youtubeViewCache[actualIndex] = newYoutubeView
                    container.addView(newYoutubeView)
                }

//                container.setOnClickListener {
//                    onMediaClick?.invoke(url, TYPE_VIDEO)
//                }
            }
        }
    }

    override fun getItemCount(): Int = mediaUrls.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
    }

    var onMediaClick: ((url: String, type: Int) -> Unit)? = null

    fun releaseAllPlayers() {
        youtubeViewCache.values.forEach { it.release() }
        youtubeViewCache.clear()
    }
//    fun releaseAllPlayers() {
//        for (i in 0 until itemCount) {
//            val holder = recyclerView?.findViewHolderForAdapterPosition(i)
//            if (holder is VideoViewHolder) {
//                holder.player?.release()
//                holder.player = null
//            }
//        }
//    }
//
//    fun releaseAllExcept(position: Int) {
//        for (i in 0 until itemCount) {
//            if (i == position) continue
//            val holder = recyclerView?.findViewHolderForAdapterPosition(i)
//            if (holder is VideoViewHolder) {
//                holder.player?.pause()
//            }
//        }
//    }
//
//    fun pauseAll() {
//        for (i in 0 until itemCount) {
//            val holder = recyclerView?.findViewHolderForAdapterPosition(i)
//            if (holder is VideoViewHolder) {
//                holder.player?.pause()
//            }
//        }
//    }
//
//    fun pause(position: Int) {
//        val holder = recyclerView?.findViewHolderForAdapterPosition(position)
//        if (holder is VideoViewHolder) {
//            holder.player?.pause()
//        }
//    }

    private fun extractYoutubeVideoId(url: String): String {
        val patterns = listOf(
            "(?<=v=)[^#&?]*",                      // standard youtube.com/watch?v=
            "(?<=be/)[^#&?]*",                     // youtu.be/
            "(?<=embed/)[^#&?]*"                   // youtube.com/embed/
        )
        for (pattern in patterns) {
            val match = pattern.toRegex().find(url)
            if (match != null) return match.value
        }
        return ""
    }

    private fun isYoutubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }
}
