package com.btb.beatthebox.ui.slot_selection

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.btb.beatthebox.R
import com.google.android.material.imageview.ShapeableImageView


class DurationSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val btnMinus: ImageButton
    private val btnPlus: ImageButton
    private val tvDuration: TextView

    private var minDuration = 60
    private var step = 30
    private var maxDuration: Int? = 18*60

    private var currentDuration = minDuration

    /** Callback to notify duration changes */
    var onDurationChanged: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_duration_selector, this)

        btnMinus = findViewById(R.id.btnMinus)
        btnPlus = findViewById(R.id.btnPlus)
        tvDuration = findViewById(R.id.tvDuration)

        updateUi()

        btnMinus.setOnClickListener {
            decrease()
        }

        btnPlus.setOnClickListener {
            increase()
        }
    }

    fun setDuration(minutes: Int) {
        currentDuration = minutes.coerceAtLeast(minDuration)
        updateUi()
    }

    fun setMaxDuration(maxMinutes: Int?) {
        maxDuration = maxMinutes
        updateUi()
    }

    fun getDuration(): Int = currentDuration

    // ---------------- INTERNAL LOGIC ----------------

    private fun increase() {
        val next = currentDuration + step
        if (maxDuration != null && next > maxDuration!!) return

        currentDuration = next
        updateUi()
        onDurationChanged?.invoke(currentDuration)
    }

    private fun decrease() {
        val next = currentDuration - step
        if (next < minDuration) {
            Toast.makeText(context,"$minDuration min is the minimum duration",Toast.LENGTH_SHORT).show()
            return
        }

        currentDuration = next
        updateUi()
        onDurationChanged?.invoke(currentDuration)
    }

    private fun updateUi() {
        tvDuration.text = "$currentDuration min"

        btnMinus.isEnabled = currentDuration > minDuration
        btnPlus.isEnabled = maxDuration == null || currentDuration < maxDuration!!
    }
}
