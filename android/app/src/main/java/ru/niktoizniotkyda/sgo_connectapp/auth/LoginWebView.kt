package ru.niktoizniotkyda.sgo_connectapp.auth

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest

class LoginWebView(
    val onLoggedIn: (userCode: Int) -> Unit,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        Log.i("WebView", "shouldOverrideUrlLoading: ${request?.url?.query}")
        if (request?.url?.scheme == "irtech" && request.url?.query?.contains("pincode") == true) {
            val code = request.url.getQueryParameter("pincode")?.toIntOrNull()
            code?.let { onLoggedIn(it) }
        }
        return false
    }
}