package ru.niktoizniotkyda.netschooltokenapp.ui

import android.util.Log

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.WindowManager
import ru.niktoizniotkyda.netschooltokenapp.LoginActivity
import ru.niktoizniotkyda.netschooltokenapp.R
import ru.niktoizniotkyda.netschooltokenapp.databinding.DialogInstructionGenTokenBinding
import ru.niktoizniotkyda.netschooltokenapp.utils.ButtonAnimator.animateButton

object InstructionGenTokenDialog {
    fun show(context: Context, activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialog = Dialog(activity)
        val dialogBinding = DialogInstructionGenTokenBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        with(dialogBinding) {
            buttonAuth.setOnClickListener {
                animateButton(it)
                val intent = Intent(activity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
                dialog.dismiss()
            }
        }

        dialog.show()
//        dialog.window?.setLayout(
//            context.resources.getDimensionPixelSize(R.dimen.warning_dialog_size),
//            context.resources.getDimensionPixelSize(R.dimen.warning_dialog_size)
//        )
        dialog.window?.setGravity(Gravity.CENTER)
    }
}