package com.main.sdkdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.main.demotoast.BlePermissions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        BlePermissions.isBleScanConditionSatisfy(this@MainActivity)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BlePermissions.BACKGROUND_LOCATION_PERMISSION_CODE -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.REQUEST_ENABLE_BT -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.LOCATION_ENABLE_REQ_CODE -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.BLE_PERMISSION_REQ_CODE_12 -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.LOCATION_PERMISSION_REQ_CODE -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BlePermissions.BACKGROUND_LOCATION_PERMISSION_CODE -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.REQUEST_ENABLE_BT -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.LOCATION_ENABLE_REQ_CODE -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.BLE_PERMISSION_REQ_CODE_12 -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
            BlePermissions.LOCATION_PERMISSION_REQ_CODE -> {
                BlePermissions.isBleScanConditionSatisfy(this@MainActivity)
            }
        }
    }

}