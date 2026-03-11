package com.btb.beatthebox.ui.slot_selection

import android.util.Log
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.SportCostDetail
import java.time.LocalDate

object PriceCalculator {

    fun calculate(
        cost: AllSportDetail, start: Int, end: Int, date: LocalDate
    ): Double {
        return calculate(
            cost.cost, cost.eveningCost.toDouble(), cost.weekendCost.toDouble(), start, end, date
        )
    }

    fun calculate(
        cost: SportCostDetail, start: Int, end: Int, date: LocalDate
    ): Double {
        return calculate(
            cost.cost, cost.eveningCost.toDouble(), cost.weekendCost.toDouble(), start, end, date
        )
    }

    private fun calculate(
        cost: Double,
        eveningCost: Double,
        weekendCost: Double,
        start: Int,
        end: Int,
        date: LocalDate
    ): Double {
        val blocks = (end - start)

        var total = blocks * cost / 2
        //check if the start time unit is greater than 24th time unit, i.e 6:00 PM
        if (start >= 24) {
            total += blocks * (eveningCost / 2)
        } else if (end > 24) {
            total += (end - 24) * (eveningCost / 2)
        }
        if (date.dayOfWeek.value >= 6) total += blocks * weekendCost / 2

        return total
    }
}
