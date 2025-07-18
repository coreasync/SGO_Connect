package ru.niktoizniotkyda.sgo_connectapp.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import ru.niktoizniotkyda.sgo_connectapp.LoginActivity
import ru.niktoizniotkyda.sgo_connectapp.R
import ru.niktoizniotkyda.sgo_connectapp.openURL
import ru.niktoizniotkyda.sgo_connectapp.databinding.DialogInstructionGenTokenBinding
import ru.niktoizniotkyda.sgo_connectapp.utils.ButtonAnimator.animateButton

object InstructionGenTokenDialog {
    fun show(context: Context, activity: AppCompatActivity) {
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

            buttonHowDoesThisWork.setOnClickListener {
                animateButton(it)
                openURL(activity,context.getString(R.string.github_button_how_does_this_work_url))
            }
        }

        dialog.show()
        dialog.window?.setGravity(Gravity.CENTER)
    }
}