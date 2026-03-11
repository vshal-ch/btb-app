package com.btb.beatthebox.ui.slot_selection.timeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.btb.beatthebox.data.model.Slot
import androidx.core.graphics.toColorInt
import com.btb.beatthebox.R
import com.btb.beatthebox.utils.ThemeUtils

class SelectionOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val minWidthPerUnit = dp(42f)
    private var widthPerUnit = dp(72f)
    private var durationUnits = 2

    private val paint = Paint().apply {
        color = "#006400".toColorInt()
    }

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = createChevronShader()
    }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A7A32")
    }

    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0A7A32")
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        strokeCap = Paint.Cap.ROUND
    }



    fun setDuration(minutes: Int) {
        durationUnits = minutes / 30
        widthPerUnit = maxOf(
            dp(72f) - (durationUnits - 2) * dp(6f), minWidthPerUnit
        )
        invalidate()
    }

    fun currentWidthPerUnit() = widthPerUnit
    fun durationUnits() = durationUnits

    override fun onDraw(canvas: Canvas) {
        val lineY = dp(60f)
        val lineWidth = durationUnits * widthPerUnit
        val left = width / 2f - lineWidth / 2f
        val right = width / 2f + lineWidth / 2f
        val lineBottom = lineY + dp(4f)
        canvas.drawRect(
            left, lineY, right, lineBottom, paint
        )
        canvas.drawRoundRect(
            left,
            lineY,
            right,
            lineBottom,
            dp(2f),
            dp(2f),
            selectionPaint
        )

        val cy = (lineY + lineY + dp(4f)) / 2f
        val radius = dp(6f)

        canvas.drawCircle(left, cy, radius, handlePaint)
        canvas.drawCircle(right, cy, radius, handlePaint)

        drawArrow(canvas,left,lineBottom+dp(12f))
        drawArrow(canvas,right,lineBottom+dp(12f))
    }

    private fun createChevronShader(): Shader {
        val size = dp(10f).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#ccffcc")
            strokeWidth = dp(2f)
            style = Paint.Style.STROKE
        }

        canvas.drawLine(0f, size / 2f, size / 2f, 0f, paint)
        canvas.drawLine(size / 2f, 0f, size.toFloat(), size / 2f, paint)
        canvas.drawLine(0f, size.toFloat(), size / 2f, size / 2f, paint)
        canvas.drawLine(size / 2f, size / 2f, size.toFloat(), size.toFloat(), paint)

        return BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }


    private fun drawArrow(
        canvas: Canvas,
        x: Float,
        y: Float,
    ) {
        val size = dp(6f)

        canvas.drawLine(
            x,
            y,
            x -  size,
            y + size,
            arrowPaint
        )
        canvas.drawLine(
            x,
            y,
            x + size,
            y + size,
            arrowPaint
        )
    }


    private fun dp(v: Float) = v * resources.displayMetrics.density
}
