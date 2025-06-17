package ru.niktoizniotkyda.netschooltokenapp

import android.annotation.SuppressLint
import android.os.Bundle
import javax.inject.Inject
import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.niktoizniotkyda.netschooltokenapp.ui.WarningDialog
import ru.niktoizniotkyda.netschooltokenapp.data.PrefsHelper
import ru.niktoizniotkyda.netschooltokenapp.data.AppDataStore
import ru.niktoizniotkyda.netschooltokenapp.utils.ButtonAnimator
import ru.niktoizniotkyda.netschooltokenapp.ui.InstructionGenTokenDialog
import ru.niktoizniotkyda.netschooltokenapp.databinding.ActivityMainBinding
import ru.niktoizniotkyda.netschooltokenapp.ui.DialogBackgroundDecorator

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var appDataStore: AppDataStore

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsHelper

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        prefs = PrefsHelper(this)

        lifecycleScope.launch {
            appDataStore.clearTokens()
        }

        if (prefs.isFirstRun()) {
            DialogBackgroundDecorator.withDimmedBackground(this@MainActivity, binding.root) {
                WarningDialog.show(this, true)
            }

            prefs.setFirstRun(false)
        }

        with(binding) {
            githubButton.setOnClickListener {
                ButtonAnimator.animateButton(it)
                openURL(getString(R.string.github_url))
            }

            tokenButton.setOnClickListener {
                ButtonAnimator.animateButton(it)
                DialogBackgroundDecorator.withDimmedBackground(this@MainActivity, binding.root) {
                    InstructionGenTokenDialog.show(this@MainActivity, this@MainActivity)
                }
            }

            warningButton.setOnClickListener {
                ButtonAnimator.animateButton(it)
                DialogBackgroundDecorator.withDimmedBackground(this@MainActivity, binding.root) {
                        WarningDialog.show(this@MainActivity, false)
                }
            }
        }

        setContentView(binding.root)
    }

    private fun openURL(@Suppress("SameParameterValue") url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}