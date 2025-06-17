package ru.niktoizniotkyda.netschooltokenapp.auth

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebStorage
import kotlinx.coroutines.CoroutineScope
import dagger.hilt.android.AndroidEntryPoint
import ru.niktoizniotkyda.netschooltokenapp.R
import androidx.navigation.fragment.findNavController
import ru.niktoizniotkyda.netschooltokenapp.databinding.WebViewLoginBinding

@AndroidEntryPoint
class WebViewLogin : Fragment(R.layout.web_view_login) {
    private lateinit var binding: WebViewLoginBinding

    private val webView = LoginWebView { userCode ->
        findNavController().navigate(
            R.id.action_webViewLogin_to_gosuslugiResult,
            bundleOf("code" to userCode)
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = WebViewLoginBinding.bind(view)

        with(binding.webView) {
            webViewClient = webView

            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE

            // Очистка кеша, истории и куков
            clearCache(true)
            clearHistory()

            CookieManager.getInstance().apply {
                removeAllCookies(null)
                flush()
            }

            WebStorage.getInstance().deleteAllData()
        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.webView.loadUrl("${getString(R.string.region_url)}login?mobile")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}