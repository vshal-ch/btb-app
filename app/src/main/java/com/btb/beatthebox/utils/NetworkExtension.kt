package com.btb.beatthebox.utils


import org.json.JSONObject
import retrofit2.Response

fun <T> Response<T>.errorMessage(): String {
    return try {
        val errorJson = errorBody()?.string()
        if (!errorJson.isNullOrEmpty()) {
            JSONObject(errorJson).optString("error", message())
        } else {
            message()
        }
    } catch (e: Exception) {
        message()
    }
}
