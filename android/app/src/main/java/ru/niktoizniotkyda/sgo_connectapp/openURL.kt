package ru.niktoizniotkyda.sgo_connectapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

fun openURL(activity: AppCompatActivity, @Suppress("SameParameterValue") url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    activity.startActivity(intent)
}