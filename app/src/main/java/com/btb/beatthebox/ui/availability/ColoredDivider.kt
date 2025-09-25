package com.btb.beatthebox.ui.availability

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

class ColoredDivider(
    context: Context,
    private val dividerColor: Int,
    private val thickness: Int,
    private val marginStartDp: Int
) : RecyclerView.ItemDecoration() {
    private val paint = Paint().apply {
        style = Paint.Style.FILL
        this.color = dividerColor
    }
    private val marginStartPx: Int =
        (marginStartDp * context.resources.displayMetrics.density).toInt()

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val bottom = child.bottom + params.bottomMargin
            val top = bottom - thickness

            // divider starts after labels (64dp)
            c.drawRect(
                (parent.paddingLeft + marginStartPx).toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat(),
                paint
            )
        }
    }
}
