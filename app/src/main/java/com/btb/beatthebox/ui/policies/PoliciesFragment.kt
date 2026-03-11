package com.btb.beatthebox.ui.policies

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.btb.beatthebox.R

class PoliciesFragment : Fragment(R.layout.fragment_policies) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRow(
            view,
            R.id.privacyPolicy,
            "Privacy Policy"
        ) {
            openWebView("Privacy Policy", "https://beat-the-box.fun/privacy-policy.html")
        }

        setupRow(
            view,
            R.id.termsConditions,
            "Terms & Conditions"
        ) {
            openWebView("Terms & Conditions", "https://beat-the-box.fun/terms-and-conditions.html")
        }

        setupRow(
            view,
            R.id.refundPolicy,
            "Refund & Cancellation Policy"
        ) {
            openWebView("Refund Policy", "https://beat-the-box.fun/refund-policy.html")
        }

        setupRow(
            view,
            R.id.aboutUs,
            "About Us"
        ) {
            openWebView("About Us", "https://beat-the-box.fun/about-us.html")
        }

        setupRow(
            view,
            R.id.contactUs,
            "Contact Us"
        ) {
            openWebView("About Us", "https://beat-the-box.fun/contact-us.html")
        }
    }

    private fun setupRow(
        root: View,
        rowId: Int,
        title: String,
        action: () -> Unit
    ) {
        val row = root.findViewById<View>(rowId)
        val titleView = row.findViewById<TextView>(R.id.tvTitle)
        titleView.text = title
        row.setOnClickListener { action() }
    }

    private fun openWebView(title: String, url: String) {
        val bundle = Bundle().apply {
            putString("title", title)
            putString("url", url)
        }
        findNavController().navigate(
            R.id.action_policies_to_webview,
            bundle
        )
    }
}
