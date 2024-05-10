package com.github.glennfolker.kirrc.fragment

import android.bluetooth.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.fragment.app.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.glennfolker.kirrc.*
import com.github.glennfolker.kirrc.fragment.ConnectFragment.ConnectAdapter.*

class ConnectFragment: Fragment(R.layout.fragment_connect) {
    val adapter: ConnectAdapter = ConnectAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        val recycler = view.findViewById<RecyclerView>(R.id.list_connect)
        recycler.adapter = adapter

        return view
    }

    inner class ConnectAdapter: Adapter<ConnectHolder>() {
        private val devices: MutableList<BluetoothDevice> = mutableListOf()

        fun add(device: BluetoothDevice) {
            val index = devices.indexOfFirst { it.address == device.address }
            if(index != -1) {
                devices[index] = device
                notifyItemChanged(index)
            } else {
                devices.add(device)
                notifyItemInserted(devices.size - 1)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ConnectHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_connect_list, viewGroup, false)
            return ConnectHolder(view)
        }

        override fun getItemCount(): Int = devices.size

        override fun onBindViewHolder(viewHolder: ConnectHolder, position: Int) {
            val device = devices[position]

            @Suppress("MissingPermission")
            viewHolder.nameText.text = device.name ?: "Unnamed"
            viewHolder.addressText.text = device.address
            viewHolder.connectButton.setOnClickListener { btnView ->
                (btnView.context as? BluetoothConnector)?.connectBluetooth(device)
            }
        }

        inner class ConnectHolder(view: View): ViewHolder(view) {
            val nameText: TextView = view.findViewById(R.id.text_name)
            val addressText: TextView = view.findViewById(R.id.text_address)
            val connectButton: Button = view.findViewById(R.id.btn_connect_list)
        }
    }
}
