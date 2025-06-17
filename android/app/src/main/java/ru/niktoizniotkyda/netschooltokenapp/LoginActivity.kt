package ru.niktoizniotkyda.netschooltokenapp

import android.os.Bundle
import androidx.navigation.NavController
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ru.niktoizniotkyda.netschooltokenapp.utils.setupInsets
import ru.niktoizniotkyda.netschooltokenapp.databinding.ContainerLoginBinding

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ContainerLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ContainerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.loginToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHost.navController

        setupInsets(binding.root)

        binding.loginToolbar.setupWithNavController(navController)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}