package com.btb.beatthebox.ui.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Faq

class FaqAdapter : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    private val items = mutableListOf<Faq>()
    private var expandedPosition = -1

    fun submitList(list: List<Faq>) {
        items.clear()
        items.addAll(list)
        expandedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = items[position]
        val isExpanded = position == expandedPosition

        holder.bind(faq, isExpanded)

        holder.itemView.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = items.size

    class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
        private val chevron: ImageView = itemView.findViewById(R.id.ivChevron)

        fun bind(faq: Faq, expanded: Boolean) {
            tvQuestion.text = faq.question
            tvAnswer.text = faq.answer
            tvAnswer.visibility = if (expanded) View.VISIBLE else View.GONE
            chevron.rotation = if (expanded) 180f else 0f
        }
    }
}
