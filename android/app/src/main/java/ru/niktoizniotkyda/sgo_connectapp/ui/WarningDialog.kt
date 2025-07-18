package ru.niktoizniotkyda.sgo_connectapp.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.CountDownTimer
import android.view.Gravity
import ru.niktoizniotkyda.sgo_connectapp.R
import ru.niktoizniotkyda.sgo_connectapp.databinding.DialogWarningBinding

object WarningDialog {
    @SuppressLint("SetTextI18n")
    fun show(context: Context, isFirstRun: Boolean) {
        val dialog = Dialog(context)
        val dialogBinding = DialogWarningBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(!isFirstRun)

        if (isFirstRun) {
            val totalSeconds = 5
            dialogBinding.buttonOk.isEnabled = false
            dialogBinding.buttonOk.text = "ОК ($totalSeconds)"

            object : CountDownTimer((totalSeconds * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = millisUntilFinished / 1000
                    dialogBinding.buttonOk.text = "ОК ($secondsLeft)"
                }
                override fun onFinish() {
                    dialogBinding.buttonOk.isEnabled = true
                    dialogBinding.buttonOk.text = "ОК"
                }
            }.start()
        } else { dialogBinding.buttonOk.isEnabled = true }

        (dialogBinding.iconWarning.drawable as? AnimatedVectorDrawable)?.start()
        dialogBinding.buttonOk.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.warning_dialog_size),
            context.resources.getDimensionPixelSize(R.dimen.warning_dialog_size)
        )
        dialog.window?.setGravity(Gravity.CENTER)
    }
}
