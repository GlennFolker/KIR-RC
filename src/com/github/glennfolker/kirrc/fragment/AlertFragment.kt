package com.github.glennfolker.kirrc.fragment

import android.app.*
import android.content.*
import android.content.DialogInterface.OnDismissListener
import android.os.*
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class AlertFragment(@StringRes private val messageId: Int, private val dismiss: OnDismissListener = OnDismissListener {}): DialogFragment() {
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismiss.onDismiss(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setMessage(messageId)
            .create()
    }
}