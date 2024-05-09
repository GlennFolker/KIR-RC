package com.github.glennfolker.kirrc.fragment

import android.Manifest.*
import android.bluetooth.*
import android.content.pm.*
import android.os.*
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.app.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.glennfolker.kirrc.*
import com.github.glennfolker.kirrc.fragment.ConnectFragment.ConnectAdapter.*

class ConnectFragment: Fragment(R.layout.fragment_connect) {
    val adapter: ConnectAdapter = ConnectAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        val recycler = view.findViewById<RecyclerView>(R.id.list_connect)
        recycler.adapter = adapter

        return view
    }

    inner class ConnectAdapter: Adapter<ConnectHolder>() {
        private val devices: ArrayList<BluetoothDevice> = ArrayList()

        fun add(device: BluetoothDevice) = devices.add(device)

        fun size() = devices.size

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConnectHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_connect_list, viewGroup, false)
            return ConnectHolder(view)
        }

        override fun getItemCount(): Int = devices.size

        override fun onBindViewHolder(viewHolder: ConnectHolder, position: Int) {
            if(ActivityCompat.checkSelfPermission(
                this@ConnectFragment.requireContext(),
                permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED) {
                viewHolder.nameText.text = devices[position].name
            }

            viewHolder.addressText.text = devices[position].address
        }

        inner class ConnectHolder(view: View): ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.text_name)
            val addressText: TextView = view.findViewById(R.id.text_address)
            val connectButton: Button = view.findViewById(R.id.btn_connect_list)
        }
    }
}