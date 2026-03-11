package com.btb.beatthebox.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.home.Banner
import com.bumptech.glide.Glide

class BannerAdapter(    private val onBannerClick: (Banner) -> Unit
) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private val items = mutableListOf<Banner>()

    fun submitList(data: List<Banner>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view,onBannerClick)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class BannerViewHolder(itemView: View,private val onClick: (Banner) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val image = itemView.findViewById<ImageView>(R.id.bannerImage)
        private val title = itemView.findViewById<TextView>(R.id.bannerTitle)


        fun bind(banner: Banner) {
            Glide.with(image)
                .load(banner.srcUrl)
                .into(image)

            title.text = banner.title

            itemView.setOnClickListener {
                onClick(banner)
            }
        }
    }
}
