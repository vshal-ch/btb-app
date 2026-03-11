package com.btb.beatthebox.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.home.Amenity
import com.google.android.material.chip.Chip

class AmenitiesAdapter :
    RecyclerView.Adapter<AmenitiesAdapter.ViewHolder>() {

    private val items = mutableListOf<Amenity>()

    fun submitList(data: List<Amenity>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val chip = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amenity, parent, false) as Chip
        return ViewHolder(chip)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val chip: Chip) : RecyclerView.ViewHolder(chip) {
        fun bind(amenity: Amenity) {
            chip.text = amenity.amenity
        }
    }
}
