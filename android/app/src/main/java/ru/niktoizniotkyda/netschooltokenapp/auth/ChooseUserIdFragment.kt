/*
 * Copyright 2024 Eugene Menshenin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.niktoizniotkyda.netschooltokenapp.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import ru.niktoizniotkyda.netschooltokenapp.R
import ru.niktoizniotkyda.netschooltokenapp.databinding.FragmentChooseUserIdBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseUserIdFragment : Fragment(R.layout.fragment_choose_user_id) {

    private lateinit var binding: FragmentChooseUserIdBinding
    private val viewModel: ChooseUserIdViewModel by viewModels()

    val adapter = UserIdAdapter {
        val loginState = arguments?.getString(GosuslugiResult.LOGIN_STATE)
        if (loginState == null) {
            viewModel.login(requireContext(), it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChooseUserIdBinding.bind(view)

        observeUsers()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeUsers() {
        viewModel.usersId.observe(viewLifecycleOwner) {
            adapter.users = it
        }
    }
}
