package com.urbitdemo.main.ble.core

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.main.demotoast.ble.atLeast
import com.urbitdemo.main.ble.Utils
import com.urbitdemo.main.ble.model.BLEScanDevice
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class BleActor(private val mContext: Context, var listener: BleConnectionListener?) {
    private var mBluetoothGatt: BluetoothGatt? = null
    private val descriptorWriteQueue: Queue<BluetoothGattDescriptor> = LinkedList()
    private var failTimer: Timer? = null
    private var failedTask: ConnectionFailedTask? = null
    var disconnectedFromDevice = false
    var bluetoothDevice: BluetoothDevice? = null
    var bleDevice: BLEScanDevice? = null
    private var executorService: ExecutorService? = Executors.newSingleThreadExecutor()
    var isFromMessage = false
    var isReconnect = false
    var isConnected = false
    var tempData = ByteArray(0)
    var attempt = 0
    var isReadRssi = false

    var isAlreadyPaired = false

    companion object {

        var bleActor: BleActor? = null
    }

    /**
     * Disconnect current device.
     */
    fun disConnectedDevice() {
        if (mBluetoothGatt != null) {
            disconnectedFromDevice = true
            refreshDeviceCache(mBluetoothGatt!!)
            if (mBluetoothGatt != null) {
                mBluetoothGatt!!.disconnect()
                /*Handler(Looper.myLooper()!!).postDelayed({
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt!!.close()
                    }
                }, 200)*/
            }
        }
    }

    /**
     * Set device in Actor
     */
    fun setDevice(device: BLEScanDevice?, isFromMessage: Boolean) {
        bleActor = this
        bleDevice = device
        bluetoothDevice = device!!.bluetoothDevice
        this.isFromMessage = isFromMessage
        executorService!!.execute {
            connectDevice()
        }

    }

    /**
     * Use to make connection to device
     */
    private fun connectDevice(): Boolean {
        Log.e(TAG, "connectDevice : $bluetoothDevice")
        if (bluetoothDevice == null) {
            listener!!.onConnectionFailed(bleScanDevice = bleDevice!!)
        }
        failTimer = Timer()
        failedTask = ConnectionFailedTask()
        failTimer!!.schedule(failedTask, 20000)
        try {
//            if (bluetoothDevice!!.bondState != BluetoothDevice.BOND_BONDED) {
//                pairedDevice(bluetoothDevice)
//            } else {
//                isAlreadyPaired = true
//            }
            mBluetoothGatt =
                bluetoothDevice!!.connectGatt(
                    mContext,
                    false,
                    mGattCallback
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * Pairing the device with remotely and received result
     *
     * @param device
     */
    private fun pairedDevice(device: BluetoothDevice?) {
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        mContext.registerReceiver(mPairingRequestReceiver, filter)
        device?.createBond()
    }


    var mPairingRequestReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(TAG, "onReceive : " + intent.action)
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
                val state =
                    intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                when (state) {
                    BluetoothDevice.BOND_BONDING -> Log.e(TAG, "BOND_BONDING")
                    BluetoothDevice.BOND_BONDED -> {
                        Log.e(TAG, "BOND_BONDED")
                        context.sendBroadcast(Intent("DevicePaired"))
                        if (descriptorWriteQueue.size > 0) {
                            Log.e("Descriptors::", "${descriptorWriteQueue.size}")
                            writeGattDescriptor(descriptorWriteQueue.element())
                        }
                        unRegisterPairedReceiver()
                    }
                    BluetoothDevice.BOND_NONE -> Log.e(TAG, "BOND_NONE")
                }
            }
        }
    }

    fun getLiveRssi() {
        isReadRssi = true
        mBluetoothGatt?.readRemoteRssi()
    }

    fun stopReadRssi() {
        isReadRssi = false
    }


    private fun unRegisterPairedReceiver() {
        try {
            mContext.unregisterReceiver(mPairingRequestReceiver)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Object of a bluetoothGattCallback
     */
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTING) {
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e(TAG, "onConnectionStateChange: STATE_CONNECTED")
                listener!!.onConnected(bluetoothDevice!!.address)
                try {
                    if (failedTask != null && failTimer != null) {
                        failTimer!!.cancel()
                        failedTask!!.cancel()
                    }
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt!!.discoverServices()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "onConnectionStateChange: STATE_DISCONNECTED")
                isConnected = false
//                if (disconnectedFromDevice) bluetoothDevice?.removeBond()
//                executorService?.shutdownNow()
                if (mBluetoothGatt != null) {
                    refreshDeviceCache(mBluetoothGatt!!)
                    mBluetoothGatt!!.close()
                    mBluetoothGatt = null
                }
                if (failedTask != null && failTimer != null) {
                    failTimer!!.cancel()
                    failedTask!!.cancel()
                }
                if (descriptorWriteQueue.size > 0) descriptorWriteQueue.clear()

                listener?.onDisconnected(bleDevice!!)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.e(TAG, "onServicesDiscovered")
            discoverServices(gatt.services)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d(
                TAG,
                "onCharacteristicRead : " + characteristic.uuid.toString() + " , data : " + Utils.bytesToHex(
                    characteristic.value
                )
            )
            if (listener != null) {
                listener!!.onCharacteristicRead(
                    bleDevice!!,
                    gatt,
                    characteristic,
                    characteristic.value
                )
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic, data: ByteArray,
            status: Int
        ) {
            if (atLeast(Build.VERSION_CODES.TIRAMISU)) {
                super.onCharacteristicRead(gatt, characteristic, data, status)
                Log.d(
                    TAG,
                    "onCharacteristicRead : " + characteristic.uuid.toString() + " , data : " + Utils.bytesToHex(
                        data
                    )
                )
                if (listener != null) {
                    listener!!.onCharacteristicRead(bleDevice!!, gatt, characteristic, data)
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (listener != null) {
                listener!!.onCharacteristicWrite(gatt = gatt, characteristic = characteristic)
            }
            Log.e(
                TAG,
                "onCharacteristicWrite : " + characteristic.uuid.toString() + " , data : " + Utils.bytesToHex(
                    characteristic.value
                )
            )
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (!atLeast(Build.VERSION_CODES.TIRAMISU)) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.d(
                    TAG,
                    "onCharacteristicChanged : " + characteristic.uuid.toString() + " , data : " + Utils.bytesToHex(
                        characteristic.value
                    )
                )

                val value = characteristic.value

                listener?.onCharacteristicChanged(
                    bluetoothDevice!!.address,
                    gatt,
                    characteristic,
                    characteristic.value,
                    bleDevice!!
                )
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
        ) {
            if (atLeast(Build.VERSION_CODES.TIRAMISU)) {
                Log.d(
                    TAG,
                    "onCharacteristicChanged : " + characteristic.uuid.toString() + " , data : " + Utils.bytesToHex(
                        value
                    )
                )
                listener?.onCharacteristicChanged(
                    bluetoothDevice!!.address,
                    gatt,
                    characteristic,
                    value,
                    bleDevice!!
                )
                super.onCharacteristicChanged(gatt, characteristic, value)
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int, data: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, data)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.e("onDescriptorWrite", "${descriptorWriteQueue.size}")
            if (descriptorWriteQueue != null && descriptorWriteQueue.size > 0) {
                descriptorWriteQueue.remove()
                if (descriptorWriteQueue.size > 0) {
                    writeGattDescriptor(descriptorWriteQueue.element())
                } else {

                    sendCommand()

                    if (listener != null) {
                        listener!!.onDescriptorWrite(bleDevice!!, this@BleActor)
                        isConnected = true
                    }
                }
            }

//            Handler(Looper.getMainLooper()).postDelayed({
//                readServiceData(BleService.INFO_SERVICE_UUID, BleService.FW_CHAR_UUID)
//            }, 2000)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.e("MTU Size: ", "" + mtu)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            Handler(Looper.getMainLooper()).postDelayed({
                if (isReadRssi) {
                    gatt?.readRemoteRssi()
                }
                Log.e("onReadRemoteRssi:: ", bleDevice?.macAddress + rssi)

            }, 1000)
        }
    }

    private fun sendCommand() {

        var data: ByteArray = ByteArray(2)
        data[0] = 0xf5.toByte()
        data[1] = 0x00.toByte()
/*
        writeServiceData(
            serUUID = BleService.CHECK_SERVICE_UUID,
            charUUID = BleService.WRITE_CHAR_UUID,
            data = data,
            attempt = 0
        )*/
    }

    /**
     * Discover the services of Connected BLE device.
     */
    private fun discoverServices(services: List<BluetoothGattService>?) {
        val serviceList = services as ArrayList<BluetoothGattService>?
        if (services != null && serviceList!!.size > 0) {
            for (gattService in serviceList) {
                Log.e("SERVICE_UUID", gattService.uuid.toString())

                    Log.e(
                        TAG,
                        "service : " + gattService.uuid.toString() + " " + bleDevice?.macAddress
                    )
                    val characteristics =
                        gattService.characteristics as ArrayList<BluetoothGattCharacteristic>
                    if (characteristics != null && characteristics.size > 0) {
                        for (i in characteristics.indices) {
                            val characteristic = characteristics[i]
                            if (characteristic != null && (isCharacteristicNotifiable(characteristic) || isCharacteristicIndicate(
                                    characteristic
                                ))
                            ) {
                                Log.d(TAG, "characteristic : " + characteristic.uuid.toString())
                                mBluetoothGatt!!.setCharacteristicNotification(characteristic, true)
                                val gattDescriptor =
                                    characteristic.descriptors as ArrayList<BluetoothGattDescriptor>
                                descriptorWriteQueue.addAll(gattDescriptor)
                            }
                        }
                    }

            }

            /*if (isFlag) {
                AppLog.addDebugStatusLog("Service is similer to UrBit service")
                Log.e("Descriptors::", "${descriptorWriteQueue.size}")
                if (descriptorWriteQueue.size > 0) {
                    Log.e("Descriptors::", "${descriptorWriteQueue.size}")
//            if (isAlreadyPaired) {
                    writeGattDescriptor(descriptorWriteQueue.element())
//            }
                } else {
                    if (listener != null) {
                        listener!!.onDescriptorWrite(this.bleDevice!!, this)
                    }
                }
            }else{
                AppLog.addDebugStatusLog("Services are not similar")
                bleDevice?.isNotOurDevice = IsOurDeviceOrNot.NOT_OUR_DEVICE
                disConnectedDevice()
            }*/

            if (listener != null) {
                listener!!.onServiceDiscovered(bluetoothDevice!!.address)
            }
        } else {
            Log.e("SERVICE_UUID", "No found")
        }
    }

    /**
     * This method is used to write descriptor of gatt
     */
    private fun writeGattDescriptor(d: BluetoothGattDescriptor) {
        Log.e(TAG, "writeGattDescriptor:: ${d.characteristic.uuid.toString()}")
        if (isCharacteristicNotifiable(d.characteristic)) {
            d.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        } else {
            d.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        }
        mBluetoothGatt!!.writeDescriptor(d)
    }

    /**
     * Check characteristic notifiable or not
     */
    private fun isCharacteristicNotifiable(pChar: BluetoothGattCharacteristic): Boolean {
        return pChar.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    }

    /**
     * Check characteristic can indicate or not
     */
    private fun isCharacteristicIndicate(pChar: BluetoothGattCharacteristic): Boolean {
        return pChar.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
    }


    /**
     * Device connection timeout call back
     */
    internal inner class ConnectionFailedTask : TimerTask() {
        override fun run() {
            failTimer!!.cancel()
            failedTask!!.cancel()
            if (listener != null) {
                listener!!.onConnectionFailed(bleDevice!!)
            }
        }
    }

    /**
     * Refresh device bluetooth gatt cache
     */
    private fun refreshDeviceCache(gatt: BluetoothGatt) {
        try {
            val localMethod =
                gatt.javaClass.getMethod("refresh", *arrayOfNulls(0))
            localMethod.invoke(gatt, *arrayOfNulls(0))
        } catch (localException: Exception) {
        }
    }


    /**
     * User read data from device
     */
    fun readServiceData(serUUID: String, charUUID: String) {
        Log.d(TAG, "readServiceData : serUUID : $serUUID, charUUID:$charUUID")
        if (mBluetoothGatt != null) {
            val service = mBluetoothGatt!!.getService(UUID.fromString(serUUID))
            if (service != null) {
                val characteristic = service.getCharacteristic(UUID.fromString(charUUID))
                if (characteristic != null) {
                    mBluetoothGatt!!.readCharacteristic(characteristic)
                }
            }
        }
    }

    /**
     * User write data to device
     */
    fun writeServiceData(
        serUUID: String,
        charUUID: String,
        data: ByteArray?,
        attempt: Int
    ): Boolean {
        if (data != null) {
            Log.d(
                TAG,
                "writeServiceData : serUUID : $serUUID, charUUID:$charUUID, data :" + Utils.bytesToHex(
                    data
                )
            )
            if (mBluetoothGatt != null) {
                val service = mBluetoothGatt!!.getService(UUID.fromString(serUUID))
                if (service != null) {
                    val characteristic = service.getCharacteristic(UUID.fromString(charUUID))
                    if (characteristic != null) {
                        characteristic.value = data
                        return mBluetoothGatt!!.writeCharacteristic(characteristic)
                    }
                } else {
                    bluetoothDevice!!.connectGatt(mContext, false, mGattCallback)
                    this.attempt = attempt + 1
                    tempData = data
                    isReconnect = true
                }
                return true
            } else {
                try {
                    mBluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        bluetoothDevice!!.connectGatt(
                            mContext,
                            false,
                            mGattCallback,
                            BluetoothDevice.TRANSPORT_LE
                        )
                    } else {
                        bluetoothDevice!!.connectGatt(mContext, false, mGattCallback)

                    }
                    this.attempt = attempt + 1
                    tempData = data
                    isReconnect = true
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return false
    }

    /**
     * Interface To Send Callback of Connection Status & Read Data Result to service
     */
    interface BleConnectionListener {
        fun onConnected(macAddress: String?)
        fun onDisconnected(bleScanDevice: BLEScanDevice)
        fun onServiceDiscovered(macAddress: String?)
        fun onDescriptorWrite(bleScanDevice: BLEScanDevice, bleActor: BleActor)
        fun onConnectionFailed(bleScanDevice: BLEScanDevice)

        fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        )

        fun onCharacteristicChanged(
            macAddress: String?,
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            bleScanDevice: BLEScanDevice
        ) {

        }

        fun onCharacteristicRead(
            bleScanDevice: BLEScanDevice,
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data: ByteArray
        ) {

        }
    }

}