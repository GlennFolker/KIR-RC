package com.github.glennfolker.kirrc

import android.bluetooth.*

interface BluetoothConnector {
    fun request()

    fun cancel()

    fun connect(device: BluetoothDevice)

    fun command(x: Int, y: Int)
}
