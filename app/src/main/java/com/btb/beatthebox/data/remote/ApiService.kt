package com.btb.beatthebox.data.remote

import com.btb.beatthebox.data.model.Contact
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.model.Venue
import com.btb.beatthebox.utils.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("slots")
    suspend fun getSlots(
        @Query("current_time") currentTime: Int,
        @Query("date") date: String,            // format: yyyy-MM-dd
    ): Response<ApiResponse<List<Slot>>>

    @GET("venue")
    suspend fun getVenue(): Response<ApiResponse<Venue>>

    @GET("contact")
    suspend fun getContacts(): Response<ApiResponse<List<Contact>>>
}