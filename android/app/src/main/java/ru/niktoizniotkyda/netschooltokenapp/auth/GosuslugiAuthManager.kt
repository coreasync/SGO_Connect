package ru.niktoizniotkyda.netschooltokenapp.auth

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class GosuslugiAuthResult(
    val users: List<UserInfo>,
    val cookies: String? = null,
    val authToken: String? = null
)

sealed class AuthError : Exception() {
    data class NetworkError(override val message: String) : AuthError()
    data class UserCancelledError(override val message: String) : AuthError()
    data class InvalidCodeError(override val message: String) : AuthError()
}

class GosuslugiAuthManager @Inject constructor(
    private val loginRepository: LoginRepository,
    private val utilsRepository: UtilsRepository
) {

    companion object {
        public const val SCHEME_IRTECH = "irtech"
        public const val PARAM_PINCODE = "pincode"
        public const val LOGIN_PATH = "authorize/login?mobile"
    }

    /**
     * Основная функция для авторизации через Госуслуги
     * @param context - контекст для создания WebView
     * @param lifecycleOwner - для управления жизненным циклом
     * @param regionUrl - URL региона для авторизации
     * @return GosuslugiAuthResult с пользователями и токенами
     */
    suspend fun authorizeWithGosuslugi(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        regionUrl: String
    ): GosuslugiAuthResult {
        return withContext(Dispatchers.Main) {
            val authCode = showWebViewAndWaitForCode(context, lifecycleOwner, regionUrl)
            withContext(Dispatchers.IO) {
                performLogin(authCode)
            }
        }
    }

    /**
     * Показывает WebView и ожидает получение кода авторизации
     */
    private suspend fun showWebViewAndWaitForCode(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        regionUrl: String
    ): Int = suspendCancellableCoroutine { continuation ->

        val authUrl = buildAuthUrl(regionUrl)

        // Получаем FragmentManager из lifecycleOwner
        val fragmentManager = when (lifecycleOwner) {
            is androidx.fragment.app.FragmentActivity -> lifecycleOwner.supportFragmentManager
            is androidx.fragment.app.Fragment -> lifecycleOwner.parentFragmentManager
            else -> throw IllegalArgumentException("LifecycleOwner должен быть FragmentActivity или Fragment")
        }

        val dialog = GosuslugiAuthDialog.newInstance(authUrl)
        dialog.setContinuation(continuation)

        // Обработка отмены
        continuation.invokeOnCancellation {
            if (dialog.isAdded) {
                dialog.dismiss()
            }
        }

        dialog.show(fragmentManager, "gosuslugi_auth")
    }

    /**
     * Выполняет авторизацию с полученным кодом
     */
    private suspend fun performLogin(authCode: Int): GosuslugiAuthResult {
        try {
            // Выполняем авторизацию
            loginRepository.login(authCode)

            // Получаем список пользователей
            val users = utilsRepository.getUsers()

            if (users.isEmpty()) {
                throw AuthError.InvalidCodeError("Не удалось получить список пользователей")
            }

            // Можно добавить извлечение куки/токенов если нужно
            val cookies = extractCookies()
            val authToken = extractAuthToken()

            return GosuslugiAuthResult(
                users = users,
                cookies = cookies,
                authToken = authToken
            )

        } catch (e: Exception) {
            when (e) {
                is AuthError -> throw e
                else -> throw AuthError.NetworkError("Ошибка авторизации: ${e.message}")
            }
        }
    }

    private fun configureWebViewSettings(webView: WebView) {
        with(webView.settings) {
            javaScriptEnabled = true
            loadsImagesAutomatically = true
            allowFileAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
    }

    private fun buildAuthUrl(regionUrl: String): String {
        return if (regionUrl.endsWith("/")) {
            "$regionUrl$LOGIN_PATH"
        } else {
            "$regionUrl/$LOGIN_PATH"
        }
    }

    private fun extractCookies(): String? {
        // Реализуйте извлечение куки если нужно
        return null
    }

    private fun extractAuthToken(): String? {
        // Реализуйте извлечение токена если нужно
        return null
    }
}

/**
 * WebViewClient для перехвата авторизации Госуслуг
 */
class GosuslugiWebViewClient(
    private val onAuthCodeReceived: (Int) -> Unit
) : android.webkit.WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: android.webkit.WebResourceRequest?
    ): Boolean {
        request?.url?.let { url ->
            if (url.scheme == GosuslugiAuthManager.SCHEME_IRTECH &&
                url.query?.contains(GosuslugiAuthManager.PARAM_PINCODE) == true) {

                val code = url.getQueryParameter(GosuslugiAuthManager.PARAM_PINCODE)?.toIntOrNull()
                code?.let { onAuthCodeReceived(it) }
                return true
            }
        }
        return false
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: android.webkit.SslErrorHandler?,
        error: android.net.http.SslError?
    ) {
        // В продакшене нужна правильная обработка SSL ошибок
        handler?.proceed()
    }
}