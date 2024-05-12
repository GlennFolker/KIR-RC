package com.github.glennfolker.kirrc.fragment

import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.*

class RequestFragment: Fragment(R.layout.fragment_request) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.findViewById<Button>(R.id.btn_try_pair)?.setOnClickListener {
            (context as? BluetoothConnector)?.request()
        }

        return view
    }
}
