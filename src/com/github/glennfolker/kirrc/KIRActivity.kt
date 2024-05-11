package com.github.glennfolker.kirrc

import android.Manifest.*
import android.bluetooth.*
import android.content.*
import android.os.*
import android.os.Build.*
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.*
import androidx.core.content.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.fragment.*
import java.io.*
import java.util.*

class KIRActivity: AppCompatActivity(R.layout.activity_kir), BluetoothConnector {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothOutput: OutputStream? = null

    private val enableBluetooth: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val list = ConnectFragment()
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.root_fragment, list)
            }

            @Suppress("MissingPermission")
            bluetoothAdapter.bondedDevices
                .filter { it.name.contains("KIR") }
                .forEach { list.adapter.add(it) }
        } else {
            AlertFragment(R.string.bluetooth_denied_request).show(supportFragmentManager, "fragment-bluetooth-denied-request")
        }
    }

    private val requestBluetooth: ActivityResultLauncher<String> = registerForActivityResult(RequestPermission()) { granted ->
        if(granted) {
            enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            AlertFragment(R.string.bluetooth_denied_permission).show(supportFragmentManager, "fragment-bluetooth-denied-permission")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = ContextCompat.getSystemService(this, BluetoothManager::class.java)?.adapter
        if(adapter == null) {
            AlertFragment(R.string.bluetooth_unsupported) { finishAndRemoveTask() }.show(supportFragmentManager, "fragment-bluetooth-unsupported")
        }

        bluetoothAdapter = adapter!!
        if(savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<RequestFragment>(R.id.root_fragment)
            }
        }
    }

    override fun requestBluetooth() {
        // On API level 31+, ask for permission first.
        if(VERSION.SDK_INT >= VERSION_CODES.S) {
            requestBluetooth.launch(permission.BLUETOOTH_CONNECT)
        } else {
            enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    override fun connectBluetooth(device: BluetoothDevice) {
        val load = LoadFragment()
        load.show(supportFragmentManager, "fragment-load")

        Thread {
            @Suppress("MissingPermission")
            try {
                // The passed UUID is the defined UUID for HC-05 RFCOMM socket(s).
                val socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                socket.connect()

                bluetoothOutput = socket.outputStream
                bluetoothSocket = socket

                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<ControlFragment>(R.id.root_fragment)
                }
            } catch(e: Exception) {
                //TODO alert can't connect

                bluetoothSocket?.run { close() }
                bluetoothSocket = null
                bluetoothOutput = null
            }

            load.dismiss()
        }.start()
    }

    @Suppress("MissingPermission")
    override fun commandBluetooth(x: Int, y: Int) {
        try {
            bluetoothOutput?.write(
                // In `0b1111`, `0b0011` is used as X delta and `0b1100` is used as Y delta.
                // A value of `0b00` means zer, `0b01` means positive, and `0b10` means negative.
                when(x) {
                    1 -> 0b01
                    -1 -> 0b10
                    else -> 0b00
                } or
                (when(y) {
                    1 -> 0b01
                    -1 -> 0b10
                    else -> 0b00
                } shl 2)
            )
        } catch(e: IOException) {
            bluetoothSocket?.close()
            bluetoothSocket = null
            bluetoothOutput = null

            //TODO alert disconnected
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<RequestFragment>(R.id.root_fragment)
            }
        }
    }
}
