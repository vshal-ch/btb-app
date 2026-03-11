package com.btb.beatthebox.ui.payments

import com.btb.beatthebox.R
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class VerifyingPaymentFragment : Fragment(R.layout.fragment_verifying_payment) {

    private val viewModel: VerifyingPaymentViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val orderId = requireArguments().getString("orderId") ?: return

        observeState()
        viewModel.startVerification(orderId)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                VerifyState.Verifying -> {
                    // no-op
                }

                is VerifyState.Success -> {
                    //toast
                    Toast.makeText(requireContext(), "Payment Successful", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(
                        R.id.action_verifyingPaymentFragment_to_paymentResultFragment, bundleOf(
                            "result" to "SUCCESS", "bookingId" to state.verifyResponse.bookingId
                        )
                    )
                }

                VerifyState.Failed, VerifyState.Timeout -> {
                    Toast.makeText(requireContext(), "Payment Failed", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(
                        R.id.action_verifyingPaymentFragment_to_paymentResultFragment, bundleOf(
                            "result" to "FAILURE", "bookingId" to null
                        )
                    )
                }
            }
        }
    }
}
