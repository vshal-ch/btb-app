package com.btb.beatthebox.ui.slot_selection

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.ui.about.sports.SportIcon
import com.btb.beatthebox.utils.ThemeUtils
import com.google.android.material.card.MaterialCardView

class SportSelectorAdapter(
    private val onClick: (AllSportDetail) -> Unit
) : RecyclerView.Adapter<SportSelectorAdapter.VH>() {

    private val items = mutableListOf<AllSportDetail>()
    private var selectedId: String? = null

    fun submit(list: List<AllSportDetail>) {
        items.clear()
        items.addAll(list)
        if (selectedId == null && list.isNotEmpty()) {
            selectedId = list.first().id   // default selection
        }
        notifyDataSetChanged()
    }

    fun setSelectedSport(sport: AllSportDetail){
        selectedId = sport.id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sport_selector, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val isSelected = item.id == selectedId

        holder.bind(item, isSelected)

        holder.itemView.setOnClickListener {
            if (selectedId == item.id) return@setOnClickListener
            selectedId = item.id
            notifyDataSetChanged()
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        private val card: MaterialCardView =
            view.findViewById(R.id.cardSport)
        private val icon: ImageView =
            view.findViewById(R.id.ivSportIcon)
        private val name: TextView =
            view.findViewById(R.id.tvSportName)

        fun bind(item: AllSportDetail, selected: Boolean) {

            name.text = item.sport

            val sportIcon = SportIcon.fromName(item.sport)
            if (sportIcon != null) {
                icon.setImageResource(sportIcon.iconRes)
            } else {
                icon.setImageDrawable(null)
            }

            val context = itemView.context

            if (selected) {
                card.setCardBackgroundColor(
                    ThemeUtils.getColor(
                        context,android.R.attr.colorPrimary
                    )
                )
                card.strokeWidth = 0

                icon.setColorFilter(Color.WHITE)
                name.setTextColor(Color.WHITE)

            } else {
                card.setCardBackgroundColor(Color.TRANSPARENT)
                card.strokeWidth = 1
                card.strokeColor = ThemeUtils.getColor(
                    context,
                    R.attr.subTextColor
                )

                icon.setColorFilter(
                    ThemeUtils.getColor(
                        context,
                        android.R.attr.textColor
                    )
                )
                name.setTextColor(
                    ThemeUtils.getColor(
                        context,
                        android.R.attr.textColor
                    )
                )
            }
        }
    }
}
