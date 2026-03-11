package com.btb.beatthebox.ui.usergames

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.GameItem
import com.btb.beatthebox.ui.slot_selection.DateSelectorView
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class GamesAdapter(
    private val onClick: (GameItem) -> Unit
) : ListAdapter<GameItem, GamesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onClick: (GameItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvSport = itemView.findViewById<TextView>(R.id.tvSport)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        private val tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)

        fun bind(item: GameItem) {
            tvSport.text = "${item.sport} • ${item.box}"

            tvDate.text = DateSelectorView.formatSelectedDate(
                LocalDate.parse(item.date)
            )
            tvTime.text = timeLabel(item.startTime, item.endTime)
            tvPrice.text = "₹ ${item.totalAmount}"

            itemView.setOnClickListener {
                onClick(item)
            }
        }

        private fun timeLabel(start: Int, end: Int): String {
            fun fmt(unit: Int): String {
                val minutes = (unit + 12) * 30
                val h = minutes / 60
                val m = minutes % 60
                return LocalTime.of(h % 24, m)
                    .format(DateTimeFormatter.ofPattern("h:mm a"))
            }
            return "${fmt(start)} – ${fmt(end)}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GameItem>() {
        override fun areItemsTheSame(
            oldItem: GameItem,
            newItem: GameItem
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: GameItem,
            newItem: GameItem
        ): Boolean = oldItem == newItem
    }
}
