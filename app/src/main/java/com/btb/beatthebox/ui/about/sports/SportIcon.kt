package com.btb.beatthebox.ui.about.sports

import androidx.annotation.DrawableRes
import com.btb.beatthebox.R

enum class SportIcon(
    val displayName: String,
    @DrawableRes val iconRes: Int
) {
    BADMINTON(
        displayName = "Badminton",
        iconRes = R.drawable.badminton
    ),
    BOX_CRICKET(
        displayName = "Cricket",
        iconRes = R.drawable.box_cricket
    ),
    VOLLEYBALL(
        displayName = "Volleyball",
        iconRes = R.drawable.volleyball
    ),
    FOOTBALL(
        displayName = "Football",
        iconRes = R.drawable.football
    );

    companion object {
        fun fromName(name: String): SportIcon? =
            entries.find { it.displayName == name }
    }
}
