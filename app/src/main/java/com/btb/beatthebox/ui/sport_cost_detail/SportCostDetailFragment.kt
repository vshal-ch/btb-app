package com.btb.beatthebox.ui.sport_cost_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.SportCostDetail
import com.btb.beatthebox.data.viewmodel.SportCostViewModel
import com.btb.beatthebox.utils.ApiResult
import com.google.android.material.button.MaterialButton

class SportCostDetailFragment : Fragment() {

    private lateinit var viewModel: SportCostViewModel
    private lateinit var btnBook: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sport_cost, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---- Arguments ----
        val sportId = arguments?.getString("SPORT_ID")
            ?: error("SPORT_ID missing")
        val sportName = arguments?.getString("SPORT_NAME") ?: "Sport"
        val box = arguments?.getString("SPORT_BOX") ?: "Box"

        // ---- Views ----
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val tvSportName = view.findViewById<TextView>(R.id.tvSportName)
        val tvSportBox = view.findViewById<TextView>(R.id.tvSportBox)
        btnBook = view.findViewById(R.id.btnBook)

        val weekdayMorning = view.findViewById<View>(R.id.weekdayMorning)
        val weekdayEvening = view.findViewById<View>(R.id.weekdayEvening)
        val weekendMorning = view.findViewById<View>(R.id.weekendMorning)
        val weekendEvening = view.findViewById<View>(R.id.weekendEvening)

        tvSportName.text = sportName
        tvSportBox.text = box
        btnBook.isEnabled = false

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // ---- ViewModel ----
        viewModel = ViewModelProvider(this)[SportCostViewModel::class.java]

        // ---- Observe API ----
        viewModel.cost.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading,
                is ApiResult.Refreshing -> {
                    btnBook.isEnabled = false
                }

                is ApiResult.Success -> {
                    bindCost(
                        result.data,
                        weekdayMorning,
                        weekdayEvening,
                        weekendMorning,
                        weekendEvening
                    )
                    btnBook.isEnabled = true
                }

                is ApiResult.Error -> {
                    btnBook.isEnabled = false
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ---- Trigger API ----
        viewModel.loadCost(sportId)

        btnBook.setOnClickListener {
            findNavController().navigate(R.id.action_sportCost_to_slotSelection)
        }
    }

    private fun bindCost(
        cost: AllSportDetail,
        weekdayMorning: View,
        weekdayEvening: View,
        weekendMorning: View,
        weekendEvening: View
    ) {
        bindRow(
            weekdayMorning,
            "6:00 AM - 6:00 PM",
            "₹ ${cost.cost} / hour"
        )

        bindRow(
            weekdayEvening,
            "6:00 PM - 12:00 AM",
            "₹ ${cost.cost + cost.eveningCost} / hour"
        )

        bindRow(
            weekendMorning,
            "6:00 AM - 6:00 PM",
            "₹ ${cost.cost + cost.weekendCost} / hour"
        )

        bindRow(
            weekendEvening,
            "6:00 PM - 12:00 AM",
            "₹ ${cost.cost + cost.weekendCost + cost.eveningCost} / hour"
        )
    }

    private fun bindRow(
        container: View,
        time: String,
        price: String
    ) {
        container.findViewById<TextView>(R.id.tvTime).text = time
        container.findViewById<TextView>(R.id.tvPrice).text = price
    }
}
