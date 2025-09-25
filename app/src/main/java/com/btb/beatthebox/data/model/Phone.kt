package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class Phone(val id: String, val type: String, @SerializedName("phone_no") val phoneNo: String)