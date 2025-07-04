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

package ru.niktoizniotkyda.netschooltokenapp.utils

import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets.max
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams


fun AppCompatActivity.setupInsets(fragmentContainer: View) {
    val w = window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer) { view, windowInsets ->
            val insetsNavigation =
                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val topBarInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures())

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                view.updateLayoutParams<MarginLayoutParams> {
                    topMargin = insets.top
                    bottomMargin = insetsNavigation.bottom
                }
            } else {
                view.setPadding(
                    insets.left,
                    topBarInset.top,
                    insets.right,
                    max(ime, insetsNavigation).bottom
                )
            }
            WindowInsetsCompat.CONSUMED
        }
    } else {
        fragmentContainer.fitsSystemWindows = true
    }
}
