package com.urbitdemo.main.ble.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import com.urbitdemo.main.ble.core.ConnectionStatus
import java.util.concurrent.CopyOnWriteArrayList

abstract class BLEScanDevice {

    companion object {
        fun getDevice(): BLEScanDevice {
            return BLEDevice()
        }
    }

    abstract var eddystoneUUID: String?
    abstract var namespace: String?
    abstract var instance: String?
    abstract var deviceRSSI: Int
    abstract var scanResult: ScanResult?
    abstract var bluetoothDevice: BluetoothDevice?
    abstract var name: String?
    abstract var macAddress: String?
    abstract var childId: String?
    abstract var isConnecting: Boolean?
    abstract var lastFoundTime: Long?
    abstract var connectionStatus: ConnectionStatus
    abstract var rssilist: CopyOnWriteArrayList<Int>
    abstract var id: ByteArray?
    abstract var averageInterval: ArrayList<Long>?
    abstract var distance: Double?
    abstract var outRangeCounter: Int
    abstract var avargeRssi: Double
    abstract var oldRssi: Int

}