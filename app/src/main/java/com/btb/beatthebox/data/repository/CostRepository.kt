package com.btb.beatthebox.data.repository

import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.SportCostDetail
import com.btb.beatthebox.data.remote.ApiService
import com.btb.beatthebox.utils.errorMessage


class CostRepository(private val apiService: ApiService) {

    suspend fun getCostBySport(sport: String): AllSportDetail {
        val response = apiService.getCostBySport(sport)

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to fetch cost details"
                )
            }
        } else {
            throw IllegalStateException(
                "Cost API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun getAllSportDetails(): List<AllSportDetail> {
        val response = apiService.getAllSportDetail()
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to fetch cost details"
                )
            }
        } else {
            throw IllegalStateException(
                "Cost API failed: ${response.errorMessage()}"
            )
        }
    }
}
