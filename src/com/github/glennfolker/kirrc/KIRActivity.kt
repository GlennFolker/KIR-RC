package com.github.glennfolker.kirrc

import android.Manifest
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

class KIRActivity: AppCompatActivity(R.layout.activity_kir), BluetoothConnector {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val enableBluetooth: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val load = LoadFragment()
            load.show(supportFragmentManager, "fragment-load")

            //TODO iterate through paired devices
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

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        if(savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<RequestFragment>(R.id.root_fragment)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val manager = ContextCompat.getSystemService(this, BluetoothManager::class.java)
        val adapter = manager?.adapter
        if(adapter == null) {
            AlertFragment(R.string.bluetooth_unsupported) { finish() }.show(supportFragmentManager, "fragment-bluetooth-unsupported")
        }

        bluetoothAdapter = adapter!!
    }

    override fun requestBluetooth() {
        // Delegate bluetooth-enabling to a permission request on API level 31+.
        if(VERSION.SDK_INT >= VERSION_CODES.S) {
            requestBluetooth.launch(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
}
