package com.btb.beatthebox.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

object ThemeUtils {
    @ColorInt
    fun getColor(context: Context, @AttrRes attr: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return value.data
    }
}
