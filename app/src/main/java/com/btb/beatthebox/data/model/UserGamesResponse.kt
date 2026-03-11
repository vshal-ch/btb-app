package com.btb.beatthebox.data.model

data class UserGamesResponse(
    val items: List<GameItem>,
    val pagination: Pagination
)
