package com.btb.beatthebox.ui.payments

import com.btb.beatthebox.R
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaymentResultFragment : Fragment(R.layout.fragment_payment_result) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val result = requireArguments().getString("result")
        val bookingId = requireArguments().getString("bookingId")

        val ivStatus = view.findViewById<ImageView>(R.id.ivStatus)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)
        val btnRetry = view.findViewById<Button>(R.id.btnRetry)
        val btnHome = view.findViewById<Button>(R.id.btnHome)

        when (result) {
            "SUCCESS" -> {
                renderSuccess(
                    ivStatus,
                    tvTitle,
                    tvSubtitle,
                    btnRetry,
                    btnHome,
                    bookingId
                )
            }

            else -> {
                renderFailure(
                    ivStatus,
                    tvTitle,
                    tvSubtitle,
                    btnRetry,
                    btnHome
                )
            }
        }

        handleBackPress()
    }

    private fun renderFailure(
        ivStatus: ImageView,
        tvTitle: TextView,
        tvSubtitle: TextView,
        btnRetry: Button,
        btnHome: Button
    ) {
        ivStatus.setImageResource(R.drawable.ic_failure)
        tvTitle.text = "Payment failed"
        tvSubtitle.text = "We couldn't complete your payment.\nYou can try again."

        btnRetry.isVisible = true
        btnHome.isVisible = true

        btnRetry.setOnClickListener {
            findNavController().popBackStack(
                R.id.navigation_checkout,
                false
            )
        }

        btnHome.setOnClickListener {
            findNavController().popBackStack(
                R.id.navigation_home,
                false
            )
        }
    }

    private fun renderSuccess(
        ivStatus: ImageView,
        tvTitle: TextView,
        tvSubtitle: TextView,
        btnRetry: Button,
        btnHome: Button,
        bookingId: String?
    ) {
        ivStatus.setImageResource(R.drawable.ic_success)
        tvTitle.text = "Payment successful"
        tvSubtitle.text = "Your slot has been booked successfully"

        // No buttons on success screen
        btnRetry.isVisible = false
        btnHome.isVisible = false

        // Auto navigate after 2 seconds
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)

            findNavController().navigate(
                R.id.action_paymentResultFragment_to_gamePassFragment,
                bundleOf("bookingId" to bookingId)
            )
        }
    }

    private fun handleBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(
                    R.id.navigation_home,
                    false
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )
    }
}
