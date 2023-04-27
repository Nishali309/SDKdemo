package com.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse

public class BlePermissions {
    companion object {
        private var blePermissions: BlePermissions? = null

        fun get(): BlePermissions {
            if (blePermissions == null) {
                blePermissions = BlePermissions()
            }
            return blePermissions as BlePermissions
        }

        val LOCATION_PERMISSION_REQ_CODE = 111
        val BACKGROUND_LOCATION_PERMISSION_CODE = 2
        val LOCATION_ENABLE_REQ_CODE = 112

        val REQUEST_ENABLE_BT = 113
        val BLE_PERMISSION_REQ_CODE_12 = 114
        var booleanForPermition = false

        fun isBleScanConditionSatisfy(context: Activity): Boolean {
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
            Toast.makeText(context,"Permission succesfully allowed",Toast.LENGTH_SHORT).show()
            return isBleScanConditionSatisfy
        }

        fun askPermissionForBackgroundUsage(context: Activity) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setMessage(
                    "This App collects location data " +
                            "to match it to air quality data " +
                            "even when the app is closed " +
                            "or not in use but only when " +
                            "an Airlib sensor is connected " +
                            "via Bluetooth."
                )
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
//                    booleanForPermition = true
//                    isBleScanConditionSatisfy(context)
                }
                .create().show()
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
                    ),context
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

    }

    interface ListenerPermission {
        fun successPermission()
    }

}