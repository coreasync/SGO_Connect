package ru.niktoizniotkyda.netschooltokenapp.auth

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * DialogFragment для отображения авторизации Госуслуг
 */
class GosuslugiAuthDialog : DialogFragment() {

    private var webView: WebView? = null
    private var continuation: Continuation<Int>? = null
    private var authUrl: String? = null

    companion object {
        private const val ARG_AUTH_URL = "auth_url"

        fun newInstance(authUrl: String): GosuslugiAuthDialog {
            return GosuslugiAuthDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_AUTH_URL, authUrl)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Создаем простой layout с WebView
        webView = WebView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return webView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authUrl = arguments?.getString(ARG_AUTH_URL)

        webView?.let { webView ->
            configureWebView(webView)
            authUrl?.let { url ->
                webView.loadUrl(url)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Делаем диалог полноэкранным
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun configureWebView(webView: WebView) {
        with(webView.settings) {
            javaScriptEnabled = true
            loadsImagesAutomatically = true
            allowFileAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
        }

        webView.webViewClient = GosuslugiWebViewClient { authCode ->
            lifecycleScope.launch {
                continuation?.resume(authCode)
                dismiss()
            }
        }
    }

    fun setContinuation(continuation: Continuation<Int>) {
        this.continuation = continuation
    }

    override fun onDestroyView() {
        webView?.destroy()
        webView = null
        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        continuation?.resumeWithException(
            AuthError.UserCancelledError("Пользователь отменил авторизацию")
        )
    }
}