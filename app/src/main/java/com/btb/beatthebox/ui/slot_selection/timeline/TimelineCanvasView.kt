package com.btb.beatthebox.ui.slot_selection.timeline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.btb.beatthebox.data.model.Slot
import androidx.core.graphics.toColorInt
import com.btb.beatthebox.R
import com.btb.beatthebox.utils.ThemeUtils
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil
import kotlin.math.floor

class TimelineCanvasView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    // --- Timeline definition ---
    private val startHour = 6
    private val totalUnits = 36              // 6:00 AM → 12:00 AM
    private val minutesPerUnit = 30
    private var durationUnits: Int = 2
    private var prevDurationUnits: Int = 2

    // UI sizing
    private var widthPerUnit = dp(72f)
    private var prevWidthPerUnit = dp(72f)
    private val lineHeight = dp(4f)
    private val labelGap = dp(60f)
    private var bufferUnitsPerSide = 2

    // Data
    private var blockedSlots: List<Slot> = emptyList()
    internal var date: LocalDate = LocalDate.now()

    // Paints
    private val basePaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private val availablePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    private val blockedPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blocked_unit)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ThemeUtils.getColor(
            context, android.R.attr.textColor
        )
        textSize = dp(10f)
        textAlign = Paint.Align.CENTER
    }

    private val tickPaint = Paint().apply {
        color = textPaint.color
        strokeWidth = dp(1f)
    }

    fun setBlockedSlots(slots: List<Slot>, slotsDate: LocalDate) {
        blockedSlots = slots
        date = slotsDate
        invalidate()
    }

    fun setDurationUnits(units: Int,unitWidth: Float) {
        if(durationUnits == units) return
        prevDurationUnits = durationUnits
        prevWidthPerUnit = widthPerUnit
        durationUnits = units
        widthPerUnit = unitWidth
        requestLayout()
        invalidate()
    }

    fun findNextAvailableUnit(durationUnits: Int): Int {
        Log.d("TimelineCanvasView", "findNextAvailableUnit: $blockedSlots")
        Log.d("TimelineCanvasView", "findNextAvailableUnit: $durationUnits"
        )
        val lastPossibleStart = totalUnits - durationUnits
        val firstUnit = if (date == LocalDate.now()) nextPossibleUnit() else 0
        for (startUnit in firstUnit..lastPossibleStart) {
            val endUnit = startUnit + durationUnits

            val overlaps = blockedSlots.any { slot ->
                val blockedStart = slot.startTime
                val blockedEnd = slot.endTime

                // overlap check
                startUnit < blockedEnd && endUnit > blockedStart
            }

            if (!overlaps) {
                return startUnit
            }
        }

        // No available slot found → fallback to 6:00 AM
        return 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        // Width of the selected duration line
        val selectedDurationWidth = durationUnits * widthPerUnit

        // Space available to show buffer
        val availableBufferWidth = (parentWidth - selectedDurationWidth).coerceAtLeast(0f)

        // How many buffer units can fit on ONE side
        bufferUnitsPerSide = ceil(availableBufferWidth / widthPerUnit).toInt()

        // Total units including buffer on both sides
        val totalUnitsWithBuffer = totalUnits + (bufferUnitsPerSide * 2)

        val width = (totalUnitsWithBuffer * widthPerUnit).toInt()
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    // --- Drawing ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTimeline(canvas)
        drawAvailableTime(canvas)
        drawBlocked(canvas)
        drawLabels(canvas)
    }

    /**
     * Draws the base available timeline
     */
    private fun drawTimeline(canvas: Canvas) {
        val top = labelGap
        val bottom = top + lineHeight

        canvas.drawRect(
            0f, top, width.toFloat(), bottom, basePaint
        )
    }

    /**
     * Draws blocked / booked slots
     */
    private fun drawBlocked(canvas: Canvas) {
        val top = labelGap - dp(2f)
        val bottom = top + lineHeight + dp(2f)

        blockedSlots.forEach { slot ->
            val startUnit = slot.startTime
            val endUnit = slot.endTime

            val left = unitToX(startUnit)
            val right = unitToX(endUnit)

            canvas.drawRect(left, top, right, bottom, blockedPaint)
        }
    }

    /**
     * Draws time labels and tick marks
     */
    private fun drawLabels(canvas: Canvas) {
        val labelY = labelGap - dp(24f)
        val tickTop = labelGap - dp(20f)
        val tickBottom = labelGap - dp(4f)

        for (unit in 0..totalUnits) {

            val x = unitToX(unit)
            val minutesFromStart = unit * minutesPerUnit
            val label = formatTime(startHour, minutesFromStart)

            // Time label
            canvas.drawText(label, x, labelY, textPaint)

            // Vertical tick
            canvas.drawLine(x, tickTop, x, tickBottom, tickPaint)
        }
    }

    private fun drawAvailableTime(canvas: Canvas) {
        val top = labelGap
        val bottom = top + lineHeight
        if (date != LocalDate.now()) {
            canvas.drawRect(
                unitToX(0), top, unitToX(totalUnits), bottom, availablePaint
            )
            return
        }

        val nowUnit = nextPossibleUnit().coerceAtMost(totalUnits)

        if (nowUnit >= 0) {
            canvas.drawRect(
                unitToX(nowUnit), top, unitToX(totalUnits), bottom, availablePaint
            )
        }
    }

    // --- Helpers ---

    fun computeNearestUnit(scrollX: Int, parentWidth: Int): Pair<Int, Int> {
        val firstUnitScrollX = getScrollXOfUnit(0, parentWidth)
        if (scrollX <= firstUnitScrollX) return Pair(0, firstUnitScrollX)

        val lastPossibleUnitScrollX = getScrollXOfUnit(totalUnits - durationUnits, parentWidth)
        if (scrollX >= lastPossibleUnitScrollX) return Pair(
            totalUnits - durationUnits,
            lastPossibleUnitScrollX
        )

        val durationWidth = durationUnits * widthPerUnit
        val selectionStartX = parentWidth / 2f - durationWidth / 2f
        val unit = ((scrollX + selectionStartX).toDouble() / widthPerUnit).roundHalfUp()
        return Pair(
            unit - bufferUnitsPerSide,
            (unitWithBufferToX(unit).toInt() - selectionStartX.toInt())
        )
    }

//    fun durationChanged(selectedUnit: Int, parentWidth: Int): Pair<Int, Int>{
//        val curDurationWidth = durationUnits * widthPerUnit
//        val selectionStartX = parentWidth / 2f - durationWidth / 2f
//    }

    fun getScrollXOfUnit(unit: Int, parentWidth: Int): Int {
        val durationWidth = durationUnits * widthPerUnit

        val selectionStartX = parentWidth / 2f - durationWidth / 2f
        return (unitToX(unit) - selectionStartX).toInt()
    }

    private fun nextPossibleUnit(): Int {
        val hour = LocalTime.now().hour
        val minutes = hour * 60 + LocalTime.now().minute
        if (minutes <= 360) {
            return 0
        }
        return ceil((minutes - 360).toDouble() / 30).toInt()
    }

    private fun currentTimeUnit(): Int {
        val now = LocalTime.now()
        val minutesFromStart = (now.hour * 60 + now.minute) - (6 * 60)
        return ceil((minutesFromStart / 30).coerceAtLeast(0).toDouble()).toInt()
    }

    fun unitWithBufferToX(unit: Int): Float {
        return (unit) * widthPerUnit
    }

    fun unitToX(unit: Int): Float {
        return (unit + bufferUnitsPerSide) * widthPerUnit
    }

    fun xToUnit(x: Float): Int {
        val rawUnit = (x / widthPerUnit).toInt()
        return (rawUnit - bufferUnitsPerSide).coerceIn(0, totalUnits)
    }

    private fun formatTime(startHour: Int, minutes: Int): String {
        val totalMinutes = startHour * 60 + minutes
        val hour24 = (totalMinutes / 60) % 24
        val mins = totalMinutes % 60

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val suffix = if (hour24 >= 12) "PM" else "AM"
        return String.format("%d:%02d %s", hour12, mins, suffix)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun Double.roundHalfUp(): Int {
        return floor(this + 0.5).toInt()
    }
}
