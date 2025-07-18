package ru.niktoizniotkyda.sgo_connectapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean = prefs.getBoolean("is_first_run", true)
    fun setFirstRun(value: Boolean) = prefs.edit { putBoolean("is_first_run", value) }
}
