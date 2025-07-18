package ru.niktoizniotkyda.sgo_connectapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.niktoizniotkyda.sgo_connectapp.R
import ru.niktoizniotkyda.sgo_connectapp.GenTokenActivity
import ru.niktoizniotkyda.sgo_connectapp.databinding.GosuslugiResultBinding

@AndroidEntryPoint
class GosuslugiResult : Fragment(R.layout.gosuslugi_result) {
    private val viewModel by viewModels<GosuslugiResultViewModel>()
    private lateinit var binding: GosuslugiResultBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = GosuslugiResultBinding.bind(view)

        val code = arguments?.getInt("code")
        code?.let {
            lifecycleScope.launch {
                viewModel.login(it)
            }
        }

        observeLoggedIn()
    }

    private fun observeLoggedIn() {
        viewModel.loggedIn.observe(viewLifecycleOwner) {
            if (it == true && !viewModel.users.value.isNullOrEmpty()) {
                binding.result.setImageResource(R.drawable.ic_done_flat)
                binding.resultText.text = "Вход через ГосУслуги выполнен!"
                CoroutineScope(Dispatchers.IO).launch {
                    delay(3000)
                    if (viewModel.users.value!!.size > 1) {
                        findNavController().navigate(
                            R.id.action_gosuslugiResultFragment_to_chooseUserIdFragment
                        )
                    } else {
                        withContext(Dispatchers.Main) {
                            val intent = Intent(requireContext(), GenTokenActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            requireContext().startActivity(intent)
                        }
                    }
                }
            } else if (it == false) {
                binding.result.setImageResource(R.drawable.ic_error)
                binding.resultText.text = viewModel.error.value
            }
        }
    }

    companion object {
        const val LOGIN_STATE = "login_state"
        const val USER_ID = "user_id"
    }
}
