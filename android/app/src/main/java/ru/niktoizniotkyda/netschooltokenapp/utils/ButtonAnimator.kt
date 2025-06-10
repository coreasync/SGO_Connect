package ru.niktoizniotkyda.netschooltokenapp.utils

import android.view.View

object ButtonAnimator {
    fun addAnimForBtn(btn: View) {
        btn.animate().scaleX(0.92f).scaleY(0.92f).setDuration(60).withEndAction {
            btn.animate().scaleX(1f).scaleY(1f).setDuration(60).start()
        }
    }
}
