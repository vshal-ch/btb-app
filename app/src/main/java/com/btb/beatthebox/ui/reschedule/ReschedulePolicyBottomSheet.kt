package com.btb.beatthebox.ui.reschedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Note
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReschedulePolicyBottomSheet(
    private val notes: List<Note>
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.bottomsheet_reschedule_policy,
            container,
            false
        )

        val containerLayout = view.findViewById<LinearLayout>(R.id.policyContainer)

        notes.forEach { note ->
            val tv = TextView(requireContext()).apply {
                text = "• ${note.note}"
                setTextAppearance(
                    com.google.android.material.R.style.TextAppearance_Material3_BodyMedium
                )
                setPadding(0, 8, 0, 8)
            }
            containerLayout.addView(tv)
        }

        view.findViewById<Button>(R.id.btnGotIt).setOnClickListener {
            dismiss()
        }

        return view
    }
}
