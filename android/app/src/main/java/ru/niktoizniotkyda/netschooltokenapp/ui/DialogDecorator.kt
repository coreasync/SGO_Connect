package ru.niktoizniotkyda.netschooltokenapp.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.view.View
import ru.niktoizniotkyda.netschooltokenapp.R

object DialogBackgroundDecorator {
    fun withDimmedBackground(context: Context, rootView: View, block: () -> Unit) {
        val originalBackground: Drawable? = rootView.background
        val dimmedBackground = ContextCompat.getDrawable(context, R.color.dialog_dim_background)

        rootView.background = dimmedBackground
        try {
            block()
        } finally {
            rootView.background = originalBackground
        }
    }
}
