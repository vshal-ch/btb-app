package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("new_user") val newUser: Boolean,
    @SerializedName("phone_no") val phoneNo: String?
)