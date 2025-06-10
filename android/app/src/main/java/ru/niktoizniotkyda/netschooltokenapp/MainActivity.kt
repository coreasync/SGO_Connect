package ru.niktoizniotkyda.netschooltokenapp

import android.os.Bundle
import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.niktoizniotkyda.netschooltokenapp.data.PrefsHelper
import ru.niktoizniotkyda.netschooltokenapp.data.AppConstants
import ru.niktoizniotkyda.netschooltokenapp.ui.WarningDialog
import ru.niktoizniotkyda.netschooltokenapp.utils.ButtonAnimator
import ru.niktoizniotkyda.netschooltokenapp.ui.InstructionGenTokenDialog
import ru.niktoizniotkyda.netschooltokenapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsHelper

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
                InstructionGenTokenDialog.show(this@MainActivity)
//                loginWithGosuslugi()
            }

            warningButton.setOnClickListener {
                ButtonAnimator.addAnimForBtn(it)
                WarningDialog.show(this@MainActivity, false)
            }
        }

        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun openURL(@Suppress("SameParameterValue") url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}