package com.btb.beatthebox.ui.slot_selection

import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.Slot

sealed class SlotSelectionState {
    object Loading : SlotSelectionState()
    object Refreshing : SlotSelectionState()
    data class Success(
        val sportDetails: List<AllSportDetail>,
        val slots: List<Slot>
    ) : SlotSelectionState()
    data class Error(val message: String) : SlotSelectionState()
}
