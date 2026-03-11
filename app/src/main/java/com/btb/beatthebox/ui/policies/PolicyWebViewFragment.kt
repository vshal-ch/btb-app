package com.btb.beatthebox.ui.policies

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.btb.beatthebox.R

class PolicyWebViewFragment : Fragment(R.layout.fragment_policy_webview) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<WebView>(R.id.webView)
        val url = requireArguments().getString("url")!!
        val title = requireArguments().getString("title")!!

        requireActivity().title = title

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }
}
