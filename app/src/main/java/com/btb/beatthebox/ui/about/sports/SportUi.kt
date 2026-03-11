package com.btb.beatthebox.ui.about.sports

import androidx.annotation.DrawableRes

data class SportUi(
    val id: String,
    val name: String,
    @DrawableRes val icon: Int,
    val box: String
)
