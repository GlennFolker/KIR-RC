package com.github.glennfolker.kirrc

import android.Manifest.*
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.content.pm.*
import android.os.*
import android.os.Build.*
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.*
import androidx.core.app.*
import androidx.core.content.*
import androidx.fragment.app.*
import com.github.glennfolker.kirrc.fragment.*
import java.util.*

class KIRActivity: AppCompatActivity(R.layout.activity_kir), BluetoothConnector {
    companion object {
        val BLUETOOTH_UUID: UUID = UUID.fromString("0e5332cf-447c-4f20-a331-3b04719e9a91")
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothScanning: Boolean = false

    private val enableBluetooth: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if(
            result.resultCode == RESULT_OK && !bluetoothScanning &&
            (
                VERSION.SDK_INT < VERSION_CODES.S ||
                ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            )
        ) {
            bluetoothScanning = true

            val load = LoadFragment()
            load.show(supportFragmentManager, "fragment-load")

            val scanner = bluetoothAdapter.bluetoothLeScanner

            val list = ConnectFragment()
            val callback = object: ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    list.adapter.add(result.device)
                    list.adapter.notifyItemInserted(list.adapter.size() - 1)
                }

                override fun onScanFailed(errorCode: Int) {
                    throw RuntimeException("Error $errorCode")
                }
            }

            @Suppress("MissingPermission") // I *LITERALLY* just checked the permission RIGHT ABOVE YOU. Shut the hell up, IntelliJ.
            scanner.startScan(
                listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(BLUETOOTH_UUID)).build()),
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(),
                callback
            )

            supportFragmentManager.commit {
                replace(R.id.root_fragment, list)
            }

            Thread {
                Looper.prepare()

                val looper = Looper.myLooper()!!
                Handler(looper).postDelayed({
                    looper.quit()
                    bluetoothScanning = false
                    load.dismiss()

                    @Suppress("MissingPermission")
                    scanner.stopScan(callback)
                }, 5000)

                Looper.loop()
            }.start()
        } else {
            AlertFragment(R.string.bluetooth_denied_request).show(supportFragmentManager, "fragment-bluetooth-denied-request")
        }
    }

    private val requestBluetooth: ActivityResultLauncher<Array<String>> = registerForActivityResult(RequestMultiplePermissions()) { granted ->
        if(
            if(VERSION.SDK_INT >= VERSION_CODES.S) {
                (granted[permission.BLUETOOTH_CONNECT]!! && granted[permission.BLUETOOTH_SCAN]!!)
            } else {
                (granted[permission.BLUETOOTH]!! && granted[permission.BLUETOOTH_ADMIN]!!)
            }
        ) {
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
        requestBluetooth.launch(if(VERSION.SDK_INT >= VERSION_CODES.S) {
            arrayOf(permission.BLUETOOTH_CONNECT, permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(permission.BLUETOOTH, permission.BLUETOOTH_ADMIN)
        })
    }
}
