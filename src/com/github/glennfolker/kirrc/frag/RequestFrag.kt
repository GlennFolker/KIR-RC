package com.github.glennfolker.kirrc.frag

import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.*

class RequestFrag: Fragment(R.layout.frag_request) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<Button>(R.id.btn_try_pair)?.setOnClickListener {
            (activity as KIRActivity).requestBluetooth()
        }

        return view
    }
}
