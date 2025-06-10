package ru.niktoizniotkyda.netschooltokenapp.ui

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import ru.niktoizniotkyda.netschooltokenapp.R
import ru.niktoizniotkyda.netschooltokenapp.databinding.DialogInstructionGenTokenBinding

object InstructionGenTokenDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        val dialogBinding = DialogInstructionGenTokenBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        dialog.window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.instruction_gen_token_dialog_size_width),
            context.resources.getDimensionPixelSize(R.dimen.instruction_gen_token_dialog_size_height)
        )
        dialog.window?.setGravity(Gravity.CENTER)
    }
}