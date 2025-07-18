package ru.niktoizniotkyda.sgo_connectapp

import android.annotation.SuppressLint
import android.os.Bundle
import javax.inject.Inject
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.niktoizniotkyda.sgo_connectapp.ui.WarningDialog
import ru.niktoizniotkyda.sgo_connectapp.data.PrefsHelper
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStore
import ru.niktoizniotkyda.sgo_connectapp.utils.ButtonAnimator
import ru.niktoizniotkyda.sgo_connectapp.ui.InstructionGenTokenDialog
import ru.niktoizniotkyda.sgo_connectapp.databinding.ActivityMainBinding
import ru.niktoizniotkyda.sgo_connectapp.ui.DialogBackgroundDecorator

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
                openURL(this@MainActivity, getString(R.string.github_url))
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

    override fun onDestroy() {
        super.onDestroy()
    }
}