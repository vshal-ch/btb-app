package com.btb.beatthebox.ui.about.sports


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R

class SportAdapter : RecyclerView.Adapter<SportAdapter.ViewHolder>() {

    var items: List<SportUi> = emptyList()

    var onSportClick: ((sport: SportUi) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport, parent, false)
        return ViewHolder(view, onSportClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View, private val onSportClick: ((sport: SportUi) -> Unit)?) :
        RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.findViewById<ImageView>(R.id.ivIcon)
        private val name = itemView.findViewById<TextView>(R.id.tvName)

        fun bind(item: SportUi) {
            icon.setImageResource(item.icon)
            name.text = item.name

            itemView.setOnClickListener {
                onSportClick?.invoke(item)
            }
        }
    }
}
