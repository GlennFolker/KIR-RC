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
        val COMMAND_SERVICE: UUID = UUID.fromString("0e5332cf-447c-4f20-a331-3b04719e9a91")
        val COMMAND_CHARACTERISTIC: UUID = UUID.fromString("ceb56614-7633-4609-b552-f6770eea1c1f")
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var commandGatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null

    private var scanning: Boolean = false

    private val enableBluetooth: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if(
            result.resultCode == RESULT_OK && !scanning &&
            (
                VERSION.SDK_INT < VERSION_CODES.S ||
                ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            )
        ) {
            scanning = true

            val load = LoadFragment()
            load.show(supportFragmentManager, "fragment-load")

            val scanner = bluetoothAdapter.bluetoothLeScanner
            val list = ConnectFragment()

            val callback = object: ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    list.adapter.add(result.device)
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e("ScanCallback", "Scanning bluetooth failed, error code $errorCode.")
                }
            }

            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.root_fragment, list)
            }

            @Suppress("MissingPermission")
            scanner.startScan(
                listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(COMMAND_SERVICE)).build()),
                ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build(),
                callback
            )

            Thread {
                Looper.prepare()

                val looper = Looper.myLooper()!!
                Handler(looper).postDelayed({
                    @Suppress("MissingPermission")
                    scanner.stopScan(callback)

                    looper.quit()
                    load.dismiss()
                    scanning = false
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
        val load = LoadFragment()
        load.show(supportFragmentManager, "fragment-load")

        @Suppress("MissingPermission")
        commandGatt = device.connectGatt(this, false, object: BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    if(newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt?.discoverServices()
                    }
                } else {
                    load.dismiss()

                    gatt?.close()
                    commandGatt = null

                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<RequestFragment>(R.id.root_fragment)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                load.dismiss()
                commandCharacteristic = gatt?.getService(COMMAND_SERVICE)?.getCharacteristic(COMMAND_CHARACTERISTIC)

                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<ControlFragment>(R.id.root_fragment)
                }
            }
        })
    }

    @Suppress("MissingPermission")
    override fun commandBluetooth(x: Int, y: Int) {
        commandGatt?.run {
            commandCharacteristic?.let {
                val payload = byteArrayOf(
                    when(x) {
                        1 -> 0b01
                        -1 -> 0b10
                        else -> 0b00
                    },
                    when(y) {
                        1 -> 0b01
                        -1 -> 0b10
                        else -> 0b00
                    }
                )

                @Suppress("Deprecation")
                if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    writeCharacteristic(it, payload, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                } else {
                    it.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    it.value = payload
                    writeCharacteristic(it)
                }
            }
        }
    }
}
