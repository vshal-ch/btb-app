package com.btb.beatthebox.ui.slot_selection.timeline

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class NoFlingHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    override fun fling(velocityX: Int) {
        // Do not call super.fling(velocityX)
        // This overrides the default fling behavior with an empty one.
    }
}
