package com.btb.beatthebox.ui.about.sports

import com.btb.beatthebox.data.model.Sport

object SportUiMapper {

    fun map(sports: List<Sport>): List<SportUi> {
        return sports.mapNotNull { sport ->
            SportIcon.fromName(sport.name)?.let {
                SportUi(
                    id = sport.id,
                    name = it.displayName,
                    icon = it.iconRes,
                    box = sport.box
                )
            }
        }
    }
}
