package ru.niktoizniotkyda.netschooltokenapp

import android.os.Bundle
import javax.inject.Inject
import android.widget.Toast
import android.content.Intent
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import ru.niktoizniotkyda.netschooltokenapp.auth.AuthError
import ru.niktoizniotkyda.netschooltokenapp.auth.AppSettings
import ru.niktoizniotkyda.netschooltokenapp.ui.WarningDialog
import ru.niktoizniotkyda.netschooltokenapp.data.PrefsHelper
import ru.niktoizniotkyda.netschooltokenapp.data.AppConstants
import ru.niktoizniotkyda.netschooltokenapp.utils.ButtonAnimator
import ru.niktoizniotkyda.netschooltokenapp.auth.SettingsDataStore
import ru.niktoizniotkyda.netschooltokenapp.auth.GosuslugiAuthResult
import ru.niktoizniotkyda.netschooltokenapp.auth.GosuslugiAuthManager
import ru.niktoizniotkyda.netschooltokenapp.ui.InstructionGenTokenDialog
import ru.niktoizniotkyda.netschooltokenapp.databinding.ActivityMainBinding

//@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsHelper

    @Inject
    lateinit var gosuslugiAuthManager: GosuslugiAuthManager

    @Inject
    lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        prefs = PrefsHelper(this)

        if (prefs.isFirstRun()) {
            WarningDialog.show(this, true)

            prefs.setFirstRun(false)
        }

        with(binding) {
            ButtonAnimator.addAnimForBtn(githubButton)
            ButtonAnimator.addAnimForBtn(tokenButton)
            ButtonAnimator.addAnimForBtn(warningButton)

            githubButton.setOnClickListener {
                ButtonAnimator.addAnimForBtn(it)
                openURL(AppConstants.GITHUB_URL)
            }

            tokenButton.setOnClickListener {
                ButtonAnimator.addAnimForBtn(it)
//                InstructionGenTokenDialog.show(this@MainActivity)
                loginWithGosuslugi()
            }

            warningButton.setOnClickListener {
                ButtonAnimator.addAnimForBtn(it)
                WarningDialog.show(this@MainActivity, false)
            }
        }

        setContentView(binding.root)
    }

    private fun openURL(@Suppress("SameParameterValue") url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    private fun loginWithGosuslugi() {
        lifecycleScope.launch {
            try {
                // Получаем URL региона
                val regionUrl = appSettings.getValue(SettingsDataStore.REGION_URL).first()
                    ?: throw IllegalStateException("Region URL не найден")

                // Выполняем авторизацию
                val result = gosuslugiAuthManager.authorizeWithGosuslugi(
                    context = this@MainActivity,
                    lifecycleOwner = this@MainActivity,
                    regionUrl = regionUrl
                )

                // Обрабатываем результат
                handleAuthResult(result)

            } catch (_: AuthError.UserCancelledError) {
                // Пользователь отменил авторизацию
                showMessage("Авторизация отменена")
            } catch (e: AuthError.NetworkError) {
                // Ошибка сети
                showMessage("Ошибка сети: ${e.message}")
            } catch (e: Exception) {
                // Другие ошибки
                showMessage("Ошибка: ${e.message}")
            }
        }
    }

    private fun handleAuthResult(result: GosuslugiAuthResult) {
        if (result.users.size == 1) {
            // Один пользователь - сохраняем и переходим к главному экрану
            saveUserAndNavigate(result.users.first().id.toString())
        } else {
            // Несколько пользователей - показываем выбор
            showUserSelection(result.users)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveUserAndNavigate(userId: String) {
        // TODO: Implement user saving and navigation logic
        // This method should save the user and navigate to the main screen
        showMessage("User saved: $userId")
    }

    private fun showUserSelection(users: List<Any>) {
        // TODO: Implement user selection dialog
        // This method should show a dialog for selecting between multiple users
        showMessage("Multiple users found: ${users.size}")
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}