package com.github.glennfolker.kirrc

import android.bluetooth.*

interface BluetoothConnector {
    fun requestBluetooth()

    fun connectBluetooth(device: BluetoothDevice)

    fun commandBluetooth(x: Int, y: Int)
}
