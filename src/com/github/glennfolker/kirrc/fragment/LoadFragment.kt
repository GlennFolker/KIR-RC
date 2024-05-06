package com.github.glennfolker.kirrc.fragment

import android.app.*
import android.graphics.*
import android.graphics.drawable.*
import android.os.*
import android.view.*
import androidx.fragment.app.DialogFragment
import com.github.glennfolker.kirrc.*

class LoadFragment: DialogFragment(R.layout.fragment_load) {
    init {
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}
