package com.ams.propertybhandar.Activity

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.ams.propertybhandar.R

class CustomLoadingDialog(context: Context) {
    private val dialog: Dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar)
    private val progressMessage: TextView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null)
        dialog.setContentView(view)

        progressMessage = view.findViewById(R.id.progress_message)
        dialog.setCancelable(false) // Prevent dialog from being dismissed by back button
    }

    fun setMessage(message: String) {
        progressMessage.text = message
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}
