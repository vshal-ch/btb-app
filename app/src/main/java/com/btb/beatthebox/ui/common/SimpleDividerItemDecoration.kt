package com.btb.beatthebox.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt

class SimpleDividerItemDecoration(
    context: Context,
    dividerHeightDp: Float = 0.5f,
    dividerColor: String = "#BCBCBC"
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = dividerColor.toColorInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dividerHeightPx =
        (dividerHeightDp * context.resources.displayMetrics.density)

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingStart.toFloat()
        val right = (parent.width - parent.paddingEnd).toFloat()

        val childCount = parent.childCount
        val adapterItemCount = parent.adapter?.itemCount ?: 0

        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            // ❌ Skip last adapter item
            if (position == RecyclerView.NO_POSITION ||
                position == adapterItemCount - 1
            ) {
                continue
            }

            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + dividerHeightPx

            c.drawRect(left, top.toFloat(), right, bottom, paint)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        // ❌ No offset for last item
        if (position == RecyclerView.NO_POSITION || position == itemCount - 1) {
            outRect.set(0, 0, 0, 0)
        } else {
            outRect.set(0, 0, 0, dividerHeightPx.toInt())
        }
    }
}
