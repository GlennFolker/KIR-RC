package com.github.glennfolker.kirrc

import android.Manifest.*
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.content.pm.*
import android.os.*
import android.os.Build.*
import android.util.*
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
            var dismissed = false
            load.show(supportFragmentManager, "fragment-load")

            val scanner = bluetoothAdapter.bluetoothLeScanner
            val list = ConnectFragment()

            val callback = object: ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    list.adapter.add(result.device)

                    synchronized(load) {
                        if(!dismissed) {
                            dismissed = true
                            load.dismiss()
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e("ScanCallback", "Scanning bluetooth failed, error code $errorCode.")
                }
            }

            supportFragmentManager.commit {
                replace(R.id.root_fragment, list)
            }

            @Suppress("MissingPermission")
            scanner.startScan(
                listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(BLUETOOTH_UUID)).build()),
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build(),
                callback
            )

            Thread {
                Looper.prepare()

                val looper = Looper.myLooper()!!
                Handler(looper).postDelayed({
                    looper.quit()
                    bluetoothScanning = false

                    synchronized(load) {
                        if(!dismissed) {
                            dismissed = true
                            load.dismiss()
                        }
                    }

                    @Suppress("MissingPermission")
                    scanner.stopScan(callback)
                }, 10000)

                Looper.loop()
            }.start()
        } else {
            AlertFragment(R.string.bluetooth_denied_request).show(supportFragmentManager, "fragment-bluetooth-denied-request")
        }
    }

    private val requestBluetooth: ActivityResultLauncher<Array<String>> = registerForActivityResult(RequestMultiplePermissions()) { granted ->
        if(
            if(VERSION.SDK_INT >= VERSION_CODES.S) {
                granted[permission.BLUETOOTH_CONNECT]!! && granted[permission.BLUETOOTH_SCAN]!!
            } else {
                granted[permission.ACCESS_FINE_LOCATION]!!
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
            arrayOf(permission.ACCESS_FINE_LOCATION)
        })
    }

    override fun connectBluetooth(device: BluetoothDevice) {
        @Suppress("MissingPermission")
        device.connectGatt(this, false, object: BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    when(newState) {
                        BluetoothProfile.STATE_CONNECTED -> gatt.discoverServices()
                        BluetoothProfile.STATE_DISCONNECTED -> gatt.close()
                    }
                } else {
                    gatt.close()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            }
        })
    }
}
