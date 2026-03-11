package com.btb.beatthebox.ui.slot_selection.timeline

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Slot
import java.time.LocalDate


class TimelineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val scrollView: NoFlingHorizontalScrollView

    private val timelineCanvas: TimelineCanvasView
    private val selectionOverlay: SelectionOverlayView

    private var selectedStart: Int = 0

    private var isProgrammaticScroll = false
    private var scrollIdleRunnable: Runnable? = null
    private val SNAP_DELAY_MS = 100L // ms

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            snapScrollToSelectedOnFirstLayout()
        }
    }


    init {
        inflate(context, R.layout.view_timeline, this)

        scrollView = findViewById(R.id.timelineScroll)
        timelineCanvas = findViewById(R.id.timelineCanvas)
        selectionOverlay = findViewById(R.id.selectionOverlay)

        scrollView.isHorizontalFadingEdgeEnabled = true
        scrollView.setFadingEdgeLength(40)

        setupScrollSnap()
    }

    // ---------------- PUBLIC API ----------------

    fun setBlockedSlots(slots: List<Slot>, date: LocalDate) {
        timelineCanvas.setBlockedSlots(slots, date)
        snapToNearestUnit()
    }

    fun setStartUnit(unit: Int, endUnit: Int) {

        val preservedUnit = unit   // 🔥 preserve logical unit

        selectionOverlay.setDuration((endUnit - unit) * 30)
        timelineCanvas.setDurationUnits(
            endUnit - unit,
            selectionOverlay.currentWidthPerUnit()
        )

        post {
            // After layout stabilizes
            selectedStart = preservedUnit

            val targetX = timelineCanvas.getScrollXOfUnit(
                preservedUnit,
                width
            )

            val maxScrollX =
                (timelineCanvas.width - scrollView.width)
                    .coerceAtLeast(0)

            isProgrammaticScroll = true
            scrollView.scrollTo(
                targetX.coerceIn(0, maxScrollX),
                0
            )
            isProgrammaticScroll = false

            onSelectionChanged?.invoke(
                preservedUnit,
                endUnit
            )
        }
    }

    fun setDuration(minutes: Int) {

        val units = minutes / 30
        val preservedUnit = selectedStart   // 🔥 preserve logical unit

        selectionOverlay.setDuration(minutes)
        timelineCanvas.setDurationUnits(
            units,
            selectionOverlay.currentWidthPerUnit()
        )

        post {
            // After layout stabilizes
            selectedStart = preservedUnit

            val targetX = timelineCanvas.getScrollXOfUnit(
                preservedUnit,
                width
            )

            val maxScrollX =
                (timelineCanvas.width - scrollView.width)
                    .coerceAtLeast(0)

            isProgrammaticScroll = true
            scrollView.scrollTo(
                targetX.coerceIn(0, maxScrollX),
                0
            )
            isProgrammaticScroll = false

            onSelectionChanged?.invoke(
                preservedUnit,
                preservedUnit + units
            )
        }
    }

    var onSelectionChanged: ((startUnit: Int, endUnit: Int) -> Unit)? = null

    private fun setupScrollSnap() {
        scrollView.setOnScrollChangeListener { _, scrollX, _, oldScrollX, _ ->
            if (isProgrammaticScroll) return@setOnScrollChangeListener
            if (scrollX == oldScrollX) return@setOnScrollChangeListener

            // cancel any pending snap
            scrollIdleRunnable?.let { scrollView.removeCallbacks(it) }

            scrollIdleRunnable = Runnable {
                snapToNearestUnit()
            }

            scrollView.postDelayed(scrollIdleRunnable!!, SNAP_DELAY_MS)
        }

        scrollView.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    // 👇 FORCE snap when finger lifts (edge-hold case)
                    snapToNearestUnit()
                    v.performClick()
                }
            }
            false
        }

    }

    private fun snapToNearestUnit() {
        val (unit, rawTargetX) =
            timelineCanvas.computeNearestUnit(
                scrollView.scrollX,
                width
            )
        val maxScrollX =
            (timelineCanvas.width - scrollView.width)
                .coerceAtLeast(0)

        val targetX =
            rawTargetX.coerceIn(0, maxScrollX)

        selectedStart = unit
        onSelectionChanged?.invoke(
            unit,
            unit + selectionOverlay.durationUnits()
        )

        // HARD stop any fling
        scrollView.scrollTo(scrollView.scrollX, 0)

        val delta = kotlin.math.abs(targetX - scrollView.scrollX)

        if (delta < 6) {
            scrollView.scrollTo(targetX, 0)
        } else {
            val animator = ObjectAnimator.ofInt(scrollView, "scrollX", targetX)
            animator.setDuration(100) // Set duration in milliseconds for a faster scroll
            animator.start()
//            scrollView.scrollTo(targetX, 0)
        }
    }

    private fun snapScrollToSelectedOnFirstLayout() {
        val unit = timelineCanvas.findNextAvailableUnit(
            selectionOverlay.durationUnits()
        )
        val durationWidth =
            selectionOverlay.durationUnits() *
                    selectionOverlay.currentWidthPerUnit()
        //log duration units and currewidperunit
        val selectionStartX =
            width / 2f - durationWidth / 2f
        val targetX = timelineCanvas.unitToX(unit)
        val scrollToX =
            (targetX - selectionStartX).toInt()
        scrollView.scrollTo(scrollToX, 0)
        selectedStart = unit
        onSelectionChanged?.invoke(unit, unit + selectionOverlay.durationUnits())
    }

}
