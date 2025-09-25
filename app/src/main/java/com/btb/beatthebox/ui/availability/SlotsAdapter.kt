package com.btb.beatthebox.ui.availability

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Slot
import kotlin.math.max
import kotlin.math.min

class SlotsAdapter : RecyclerView.Adapter<SlotsAdapter.SlotViewHolder>() {

    private val blockedSlots = mutableListOf<Slot>()
    private val startHour = 6  // 6 AM
    private val endHour = 24   // 12 AM

    override fun getItemCount(): Int {
        return (endHour - startHour) // 18 hours (6 → 24)
    }

    fun setBlockedSlots(slots: List<Slot>) {
        blockedSlots.clear()
        blockedSlots.addAll(slots)
        notifyDataSetChanged()
    }

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hourLabel: TextView = itemView.findViewById(R.id.tvHourLabel)
        val overlayContainer: FrameLayout = itemView.findViewById(R.id.overlay_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_timeline_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val hourStart = (startHour * 60) + (position * 60) // in minutes
        val hourEnd = hourStart + 60

        // Remove old overlays
        holder.overlayContainer.removeAllViews()
        holder.hourLabel.text = formatHour(hourStart)

        // For each blocked slot, check overlap with this hour
        blockedSlots.forEach { slot ->
            val overlapStart = max(hourStart, slot.startTime)
            val overlapEnd = min(hourEnd, slot.endTime)

            if (overlapStart < overlapEnd) {
                val fractionStart = (overlapStart - hourStart) / 60f
                val fractionEnd = (overlapEnd - hourStart) / 60f
                val fractionHeight = fractionEnd - fractionStart

                val ctx = holder.itemView.context

                // Create grey overlay (added FIRST so it is below labels)
                val overlay = FrameLayout(ctx).apply {
                    setBackgroundColor(ContextCompat.getColor(ctx, R.color.divider_color))
                }

                // Clear any previous children (overlay + labels) and add overlay
                // (note: removeAllViews() already called at top of onBindViewHolder)
                holder.overlayContainer.addView(overlay)

                // Post to wait until overlayContainer has width/height
                holder.overlayContainer.post {
                    val containerHeight = holder.overlayContainer.height
                    // minimum overlay height in px (12dp)
                    val minOverlayPx = dpToPx(ctx, 12)
                    val overlayHeight =
                        max(minOverlayPx, (containerHeight * fractionHeight).toInt())

                    // set overlay size & position
                    val overlayLp = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        overlayHeight
                    )
                    overlay.layoutParams = overlayLp
                    val overlayTop = (containerHeight * fractionStart).toFloat()
                    overlay.translationY = overlayTop

                    // ---------- START LABEL ----------
                    if (slot.startTime in hourStart until hourEnd) {
                        val startLabel = TextView(ctx).apply {
                            text = formatHourWithMins(slot.startTime)
                            setTextColor(ContextCompat.getColor(ctx, R.color.white))
                            textSize = 12f
                            setPadding(
                                dpToPx(ctx, 4),
                                dpToPx(ctx, 2),
                                dpToPx(ctx, 4),
                                dpToPx(ctx, 2)
                            )
                            // Optional: small background to improve contrast
                            setBackgroundResource(R.drawable.label_bg_translucent) // create small rounded translucent bg or remove line if not needed
                        }

                        // add label *to overlayContainer* (so it won't be clipped by overlay)
                        val startLp = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.START
                        )
                        holder.overlayContainer.addView(startLabel, startLp)

                        // position label vertically at overlayTop (clamped)
                        startLabel.post {
                            val labelH = startLabel.height
                            var y = overlayTop.toInt()
                            // clamp: ensure label fully visible inside container
                            if (y + labelH > containerHeight) y = containerHeight - labelH
                            if (y < 0) y = 0
                            startLabel.translationY = y.toFloat()
                            // small left padding
                            startLabel.translationX = dpToPx(ctx, 4).toFloat()
                        }
                    }

                    // ---------- END LABEL ----------
                    if (slot.endTime in hourStart..hourEnd) {
                        val endLabel = TextView(ctx).apply {
                            text = formatHourWithMins(slot.endTime)
                            setTextColor(ContextCompat.getColor(ctx, R.color.white))
                            textSize = 12f
                            setPadding(
                                dpToPx(ctx, 4),
                                dpToPx(ctx, 2),
                                dpToPx(ctx, 4),
                                dpToPx(ctx, 2)
                            )
                            setBackgroundResource(R.drawable.label_bg_translucent)
                        }

                        val endLp = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.END
                        )
                        holder.overlayContainer.addView(endLabel, endLp)

                        endLabel.post {
                            val labelH = endLabel.height
                            var y = (overlayTop + overlayHeight - labelH).toInt()

                            if (y + labelH > containerHeight) y = containerHeight - labelH
                            if (y < 0) y = 0

                            endLabel.translationY = y.toFloat()
                            endLabel.translationX =
                                (-dpToPx(ctx, 4)).toFloat()
                        }
                    }
                }
            }
        }
    }

    private fun formatHour(minutes: Int): String {
        val hour = minutes / 60
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val suffix = if (hour >= 12) "PM" else "AM"
        return "$displayHour:00 $suffix"
    }

    private fun formatHourWithMins(minutes: Int): String {
        val hour = minutes / 60
        val mins = minutes % 60
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val suffix = if (hour >= 12) "PM" else "AM"
        return "${String.format("%02d:%02d", displayHour, mins)} $suffix"
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

}