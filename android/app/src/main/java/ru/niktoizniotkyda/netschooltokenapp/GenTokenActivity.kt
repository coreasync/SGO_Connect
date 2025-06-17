package ru.niktoizniotkyda.netschooltokenapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.niktoizniotkyda.netschooltokenapp.data.AppDataStore
import ru.niktoizniotkyda.netschooltokenapp.data.TokenData
import ru.niktoizniotkyda.netschooltokenapp.databinding.ActivityGenTokenBinding
import javax.inject.Inject

@AndroidEntryPoint
class GenTokenActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStore: AppDataStore
    private lateinit var binding: ActivityGenTokenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityGenTokenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lifecycleScope.launch {
            val token: TokenData = dataStore.getSelectedTokenData()!!
        }
    }
}
