package com.main.sdkdemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.controls.ControlsProviderService
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.main.demotoast.BlePermissions
import com.urbitdemo.main.ble.model.BLEScanDevice

class MainActivity : AppCompatActivity(), BlePermissions.Companion.ListenerDevice,
    ScanDevicesAdapter.Listener {

    lateinit var recView: RecyclerView
    val devicesList = arrayListOf<BLEScanDevice>()
    val SERVICE_UUID = "C21881E5-4E2C-4F7B-BC50-9E82A8910C14"
    val READ_SERVICE_UUID = "C8FA78D2-7B06-44CA-8A47-70748AA63E57"
    val WRITE_SERVICE_UUID = "24811361-BCAB-4B94-B6F7-DBA39DC1B67D"

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
        }, 100)

    }

    override fun ListenerMessage(msg: String) {

        this.runOnUiThread(Runnable {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        })

    }

    override fun onItemClick(position: Int, deviceModel: BLEScanDevice) {
        Log.e(ControlsProviderService.TAG, "GGGGGgggggggggggggggg")

//        BlePermissions.readServiceData1()
        BlePermissions.connectDevice(deviceModel.macAddress.toString(), this, this)
    }

    override fun onItemClickRead(position: Int, deviceModel: BLEScanDevice) {
//        BlePermissions.readServiceData1(SERVICE_UUID, READ_SERVICE_UUID)

        val bytes = byteArrayOf(0x48, 101, 108, 108, 111)
        BlePermissions.writeServiceData1(SERVICE_UUID, WRITE_SERVICE_UUID, bytes, 1)
    }

}