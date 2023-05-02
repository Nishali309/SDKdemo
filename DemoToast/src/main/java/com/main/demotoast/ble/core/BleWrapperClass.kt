package com.urbitdemo.main.ble.core

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.main.demotoast.BlePermissions

open class BleWrapperClass(context: AppCompatActivity, val callback: BlePermissions.BleScanCallBack) {

    private val TAG: String = BleWrapperClass::class.java.simpleName
    private val context = context
    private var errorText = ""
    private var noRights = false
    private var qaulId: ByteArray? = null
    private var advertMode = "low_latency"
    private var isFromMessage = false

    /**
     * Static Member Declaration
     */
    companion object {
        val serviceManager = this
        var bleWrapperClass: BleWrapperClass? = null
        const val LOCATION_PERMISSION_REQ_CODE = 111
        const val LOCATION_ENABLE_REQ_CODE = 112
        const val REQUEST_ENABLE_BT = 113
        const val BLE_PERMISSION_REQ_CODE_12 = 114
        const val LOCATION_BACKGROUND_REQ_CODE = 115
        const val IGNORE_BATTERY_OPTIMIZE_REQ_CODE = 116
        var bleScanCallBack: BlePermissions.BleScanCallBack? = null

        fun getWrapperClassObject(context: AppCompatActivity, callback: BlePermissions.BleScanCallBack ): BleWrapperClass {
            if (bleWrapperClass == null) {
                bleWrapperClass = BleWrapperClass(context, callback)
            }
            return bleWrapperClass!!
        }
    }

    /**
     * Checks if Bluetooth is Enabled or Not
     */
    private fun isBluetoothEnable(): Boolean {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        return if (bluetoothAdapter != null) {
            bluetoothAdapter.isEnabled
        } else {
            Log.e(TAG, "Bluetooth Not Supported")
            false
        }
    }

}