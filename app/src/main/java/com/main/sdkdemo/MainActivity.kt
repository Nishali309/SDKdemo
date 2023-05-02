package com.main.sdkdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.controls.ControlsProviderService
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.main.sdkdemo.ScanDevicesAdapter
import com.main.demotoast.BlePermissions
import com.urbitdemo.main.ble.model.BLEScanDevice

class MainActivity : AppCompatActivity(), BlePermissions.Companion.ListenerDevice,
    ScanDevicesAdapter.Listener {

    lateinit var recView: RecyclerView
    val devicesList = arrayListOf<BLEScanDevice>()

    companion object {
        var adapter: ScanDevicesAdapter? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        callBleFunction()

    }

    fun init() {
        recView = findViewById(R.id.rvDeviceList)

        val devices = devicesList
        adapter = ScanDevicesAdapter(this)
        adapter?.clearList()
        adapter?.notifyDataSetChanged()
        recView.adapter = adapter
    }

    fun callBleFunction() {
        BlePermissions.isBleScanConditionSatisfy(this@MainActivity, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BlePermissions.BACKGROUND_LOCATION_PERMISSION_CODE -> {
                callBleFunction()
            }
            BlePermissions.REQUEST_ENABLE_BT -> {
                callBleFunction()
            }
            BlePermissions.LOCATION_ENABLE_REQ_CODE -> {
                callBleFunction()
            }
            BlePermissions.BLE_PERMISSION_REQ_CODE_12 -> {
                callBleFunction()
            }
            BlePermissions.LOCATION_PERMISSION_REQ_CODE -> {
                callBleFunction()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BlePermissions.BACKGROUND_LOCATION_PERMISSION_CODE -> {
                callBleFunction()
            }
            BlePermissions.REQUEST_ENABLE_BT -> {
                callBleFunction()
            }
            BlePermissions.LOCATION_ENABLE_REQ_CODE -> {
                callBleFunction()
            }
            BlePermissions.BLE_PERMISSION_REQ_CODE_12 -> {
                callBleFunction()
            }
            BlePermissions.LOCATION_PERMISSION_REQ_CODE -> {
                callBleFunction()
            }
        }
    }

    override fun successDeviceFound(bleDevice: BLEScanDevice) {
//        AppLog.e(TAG , "Found Device " + bleDevice.macAddress)

        Handler(Looper.getMainLooper()).postDelayed({
            if (adapter != null) {
                adapter!!.add(bleDevice)
            }
        }, 20000)

    }

    override fun onItemClick(position: Int, deviceModel: BLEScanDevice) {
        Log.e(ControlsProviderService.TAG, "GGGGGgggggggggggggggg")

        BlePermissions.connectDevice(deviceModel.macAddress.toString(), this)
    }

}