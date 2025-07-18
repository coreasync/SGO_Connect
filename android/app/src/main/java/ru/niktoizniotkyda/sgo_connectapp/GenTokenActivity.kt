package ru.niktoizniotkyda.sgo_connectapp

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.niktoizniotkyda.sgo_connectapp.api.SGOConnectViewModel
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStore
import ru.niktoizniotkyda.sgo_connectapp.data.TokenData
import ru.niktoizniotkyda.sgo_connectapp.databinding.ActivityGenTokenBinding
import javax.inject.Inject

@AndroidEntryPoint
class GenTokenActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStore: AppDataStore
    @Inject
    lateinit var viewModel: SGOConnectViewModel
    private lateinit var binding: ActivityGenTokenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityGenTokenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        lifecycleScope.launch {
            val token: TokenData = dataStore.getSelectedTokenData()!!
            viewModel.createToken(token)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // Наблюдение за состоянием загрузки
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                showLoadingIndicator()
            } else {
                hideLoadingIndicator()
            }
        })

        // Наблюдение за ошибками
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })

        // Наблюдение за результатом создания токена
        viewModel.tokenId.observe(this, Observer { tokenId ->
            binding.textToken.text = tokenId?.token_id
            val txm = tokenId?.token_expires_seconds?.div(60)!!

            val o = if (txm == 1) { "у" } else {
                if (txm > 1 && txm < 5 ) { "ы" } else { "" }
            }

            binding.textTokenValidity.text = "Токен действителен $txm минут$o"
            val tokenView = findViewById<TextView>(R.id.textToken)
            val copyBtn = findViewById<ImageButton>(R.id.btnCopy)

            copyBtn.setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Token", tokenView.text.toString())
                clipboard.setPrimaryClip(clip)

                copyBtn.animate()
                    .scaleX(1.25f)
                    .scaleY(1.25f)
                    .setDuration(120)
                    .withEndAction {
                        copyBtn.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start()
                        copyBtn.setImageResource(R.drawable.ic_check)
                        Handler(Looper.getMainLooper()).postDelayed({
                            copyBtn.setImageResource(R.drawable.ic_copy)
                        }, 1000)
                    }.start()

                Snackbar.make(tokenView, R.string.token_copied, Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoadingIndicator() {
        binding.loading.visibility = VISIBLE
    }

    private fun hideLoadingIndicator() {
        binding.loading.visibility = GONE

        binding.textTitle.visibility = VISIBLE
        binding.tokenCard.visibility = VISIBLE
        binding.textTokenValidity.visibility = VISIBLE
    }
}
