package com.main.demotoast

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.urbitdemo.main.ble.Utils
import com.urbitdemo.main.ble.core.BleActor
import com.urbitdemo.main.ble.core.ConnectionStatus
import com.urbitdemo.main.ble.model.BLEScanDevice
import java.util.*
import kotlin.collections.ArrayList

public class BlePermissions {

    companion object {
        private var blePermissions: BlePermissions? = null

        private var bleScanner: BluetoothLeScanner? = null
        private val handler = Handler(Looper.getMainLooper())
        val devicesList = arrayListOf<BLEScanDevice>()
        private var filters: ArrayList<ScanFilter> = arrayListOf()
        private var scanSettings: ScanSettings? = null
        val actors = Collections.synchronizedMap(hashMapOf<String, BleActor>())

        val LOCATION_PERMISSION_REQ_CODE = 111
        val BACKGROUND_LOCATION_PERMISSION_CODE = 2
        val LOCATION_ENABLE_REQ_CODE = 112

        val REQUEST_ENABLE_BT = 113
        val BLE_PERMISSION_REQ_CODE_12 = 114
        var booleanForPermition = false

        fun isBleScanConditionSatisfy(context: Activity, listener: ListenerDevice): Boolean {
            // IT WILL CHECK ALL THE BLE PERMISSION ONE BYE ONE
            var isBleScanConditionSatisfy = true
            /*if (!booleanForPermition) {
                // CHECK FOR LOCATION PERMISSION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (!hasPermission(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
                        askPermissionForBackgroundUsage(context)
                        return false
                    }
                } else {
                    if (!hasPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))) {
                        askPermissionForBackgroundUsage(context)
                        return false
                    }
                }
            }*/
            if (!isBLeSupported(context)!!) {
                Log.e(TAG, "isBLeSupported() : false")
                isBleScanConditionSatisfy = false
                return false
            }

            // CHECK FOR BT PERMISSION AND IF BL IS NOT ON THEN ASK FOR PERMISSION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!isBluetoothPermissionAllowed(context)!!) {
                    Log.e(TAG, "isBluetoothPermissionGranted() : false")
                    isBleScanConditionSatisfy = false
                    enableBlePermission(context, BLE_PERMISSION_REQ_CODE_12)
                    return false
                }
            }

            // LOCATION PERMISSION ANDROID VERSION WISE
            if (!isLocationPermissionAllowed(context)!!) {
                Log.e(TAG, "isLocationPermissionAllowed() : false")
                isBleScanConditionSatisfy = false
                enableLocationPermission(context, LOCATION_PERMISSION_REQ_CODE)
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!hasPermission(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        context
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                    return false
                }
            }
            if (!isLocationEnable(context)!! && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Log.e(TAG, "isLocationEnable() : false")
                isBleScanConditionSatisfy = false
                //            val executor = Executors.newSingleThreadExecutor()
//            executor.execute {
                enableLocation(context, LOCATION_ENABLE_REQ_CODE)
                //            }
                return false
            }
            if (!isBluetoothEnable(context)!!) {
                Log.e(TAG, "isBluetoothEnable() : false")
                isBleScanConditionSatisfy = false
                enableBluetooth(context, REQUEST_ENABLE_BT)
                return false
            }
            Log.e(TAG, "isBleScanConditionSatisfy() : True.......")
//            Toast.makeText(context,"Permission succesfully allowed",Toast.LENGTH_SHORT).show()

            startScan(listener)

            return isBleScanConditionSatisfy
        }

        fun isBluetoothPermissionAllowed(context: Context): Boolean? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                hasPermission(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    ), context
                )
            } else false
        }

        fun enableBlePermission(activity: Activity, requestCode: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    requestCode
                )
            }
        }

        fun isLocationPermissionAllowed(context: Context): Boolean? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasPermission(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), context
                )
            } else false
        }

        fun enableLocationPermission(activity: Activity, requestCode: Int) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode
            )
        }

        protected fun enableLocation(context: Activity, locationReqCode: Int) {
            val locationRequest = LocationRequest.create()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            LocationServices
                .getSettingsClient(context)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(
                    context
                ) { response: LocationSettingsResponse? -> }
                .addOnFailureListener(
                    context
                ) { ex: Exception? ->
                    if (ex is ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            ex.startResolutionForResult(context!!, locationReqCode)
                        } catch (sendEx: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
        }

        @SuppressLint("MissingPermission")
        fun enableBluetooth(context: Activity, requestCode: Int): Boolean? {
            val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    context.startActivityForResult(enableBtIntent, requestCode)
                    false
                } else {
                    true
                }
            } else {
                Toast.makeText(context, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show()
                false
            }
        }

        fun isLocationEnable(context: Context): Boolean? {
            val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager
            return try {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                false
            }
        }

        fun isBluetoothEnable(context: Context): Boolean? {
            val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter?.isEnabled ?: false
        }

        fun isBLeSupported(context: Context): Boolean? {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        }

        fun hasPermission(permissions: Array<String?>?, context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) return false
                }
            }
            return true
        }

        @SuppressLint("MissingPermission")
        open fun startScan(listener: ListenerDevice) {
            //AppLog.e(TAG,"Start Scan")
            Log.e(TAG, "INSIDE SATRTSCAN OF BLE SERVICE")
            bleScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

            clearData()

            filters.clear()

            scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

            scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

            var scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
//                    Log.e(TAG, "onScanResult DEVICE FOUND : " + result)
                    result?.let {
                        val bleDevice: BLEScanDevice = BLEScanDevice.getDevice()
                        bleDevice.name = ""
                        bleDevice.macAddress = it.device.address
                        bleDevice.bluetoothDevice = it.device
                        devicesList.add(bleDevice)
//                parseBLEFrame(it.device, it.rssi, result)
                        listener.successDeviceFound(bleDevice)
                    }

                }

                override fun onScanFailed(errorCode: Int) {
                    //AppLog.e(TAG,"scan Fail"+errorCode)
                    super.onScanFailed(errorCode)
                    //AppLog.addDebugStatusLog("ON SCAN FAILED")
                    when (errorCode) {
                        SCAN_FAILED_ALREADY_STARTED -> {

                        }

                    }
                }


            }

            bleScanner?.startScan(null, scanSettings, scanCallback)

        }

        private fun clearData() {
            devicesList.clear()
        }

        fun connectDevice(macAddress: String, context: Context): BleActor? {
            val device = devicesList.find { it.macAddress == macAddress } ?: return null
            var baseBleActor = actors[macAddress]

            val connectionListener = if (baseBleActor != null) {
                baseBleActor.listener
            } else {
                object : BleActor.BleConnectionListener {
                    override fun onConnected(macAddress: String?) {
                        Log.e(TAG, "STATUS1 onConnected : $macAddress")
                        Toast.makeText(context,"Successfully Connected to : " + macAddress , Toast.LENGTH_SHORT).show()
                    }

                    override fun onDisconnected(bleScanDevice: BLEScanDevice) {
                        Log.e(TAG, "STATUS1 onDisconnected : ${bleScanDevice.macAddress}")
                    }

                    override fun onServiceDiscovered(macAddress: String?) {
                        Log.e(TAG, "STATUS1 onServiceDiscovered : $macAddress")
                    }

                    override fun onDescriptorWrite(
                        bleScanDevice: BLEScanDevice,
                        bleActor: BleActor
                    ) {
                        Log.e(TAG, "STATUS1 onDescriptorWrite : ${bleScanDevice.macAddress}")
                    }

                    override fun onConnectionFailed(bleScanDevice: BLEScanDevice) {

                    }

                    override fun onCharacteristicRead(
                        bleScanDevice: BLEScanDevice,
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        data: ByteArray
                    ) {
                        super.onCharacteristicRead(bleScanDevice, gatt, characteristic, data)

                    }

                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?
                    ) {
                        Log.e(
                            TAG,
                            "STATUS1 onCharacteristicWrite " + Utils.bytesToHex(characteristic?.value)
                        )

                    }

                    override fun onCharacteristicChanged(
                        macAddress: String?,
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray,
                        bleScanDevice: BLEScanDevice
                    ) {
                        Log.e(
                            TAG,
                            "STATUS1 onCharacteristicChanged : ${characteristic.uuid.toString()} ${
                                Utils.bytesToHex(value)
                            }"
                        )
                    }

                }
            }
            if (baseBleActor == null) {
                baseBleActor = BleActor(context, connectionListener)
            }
            baseBleActor.setDevice(device = device, isFromMessage = false)
            device.connectionStatus = ConnectionStatus.DEVICE_CONNECTING
//        sendBroadcast(action = "deviceUpdate", data = Gson().toJson(device))
            actors[device.macAddress] = baseBleActor
            return baseBleActor
        }

        interface ListenerDevice {
            fun successDeviceFound(macAddress: BLEScanDevice)
        }



    }


    interface BleScanCallBack {
        fun startScanRes(status: Boolean, errorText: String, unknownError: Boolean)
        fun stopScanRes(status: Boolean, errorText: String)
        fun deviceFound(bleDevice: BLEScanDevice)
        fun deviceOutOfRange(bleDevice: BLEScanDevice)
        fun onMessageSent(id: String, success: Boolean, data: ByteArray)
    }
}