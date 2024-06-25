package com.github.glennfolker.kirrc

import android.bluetooth.*

interface BluetoothConnector {
    fun request()

    fun cancel()

    fun connect(device: BluetoothDevice)

    fun refresh()

    fun command(x: Int, y: Int)
}
