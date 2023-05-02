package com.urbitdemo.main.ble.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Parcel
import com.urbitdemo.main.ble.core.ConnectionStatus
import java.util.concurrent.CopyOnWriteArrayList

class BLEDevice() : BLEScanDevice() {

    override var deviceRSSI: Int = 0
    override var scanResult: ScanResult? = null
    override var name: String? = null
    override var macAddress: String? = null

    override var eddystoneUUID: String? = null
    override var namespace: String? = null
    override var instance: String? = null
    override var connectionStatus: ConnectionStatus = ConnectionStatus.DEVICE_DISCONNECTED

    var intervalNanos: Long = 0
        private set
    override var bluetoothDevice: BluetoothDevice? = null
    override var isConnecting: Boolean? = false
    override var lastFoundTime: Long? = null
    override var childId: String? = ""

    override var averageInterval: ArrayList<Long>? = ArrayList()
    override var distance: Double? = 0.0
    override var avargeRssi = 0.0
    override var rssilist: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    override var id: ByteArray?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var oldRssi = 0
    override var outRangeCounter = 0

    constructor(parcel: Parcel) : this() {
        deviceRSSI = parcel.readInt()
        scanResult = parcel.readParcelable(ScanResult::class.java.classLoader)
        name = parcel.readString()
        macAddress = parcel.readString()
        eddystoneUUID = parcel.readString()
        namespace = parcel.readString()
        instance = parcel.readString()
        bluetoothDevice = parcel.readParcelable(BluetoothDevice::class.java.classLoader)
        isConnecting = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        lastFoundTime = parcel.readValue(Long::class.java.classLoader) as? Long
        childId = parcel.readString()
        distance = parcel.readValue(Double::class.java.classLoader) as? Double
        avargeRssi = parcel.readDouble()
        oldRssi = parcel.readInt()
        outRangeCounter = parcel.readInt()
    }



    override fun toString(): String {
        return "BLEScanDevice(deviceRSSI=$deviceRSSI, scanResult=$scanResult, name=$name, macAddress=$macAddress, intervalNanos=$intervalNanos, bluetoothDevice=$bluetoothDevice, isConnecting=$isConnecting, lastFoundTime=$lastFoundTime, childId=$childId)"
    }
}