package com.main.sdkdemo

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.main.sdkdemo.databinding.RowScanResultBinding
import com.urbitdemo.main.ble.model.BLEScanDevice

class ScanDevicesAdapter(listener: Listener) :
    RecyclerView.Adapter<ScanDevicesAdapter.MyViewHolder>() {

    private var listener: Listener? = listener
    private var devices = arrayListOf<BLEScanDevice>()

    class MyViewHolder(itemView: RowScanResultBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: RowScanResultBinding? = null
        init {
            binding = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding: RowScanResultBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_scan_result,
                parent,
                false
            )

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        devices[position].let {
            holder.binding?.apply {

                if (it.name?.isNotEmpty() == true) {
                    tvDeviceName.text = it.name
                } else {
                    tvDeviceName.text = "Unknown"
                }

                tvMacAddress.text = "MacAddress : " + it.macAddress
//                btnConnect.isClickable = true

                btnConnect.setOnClickListener {

                        Log.e(TAG, "GGGGGgggggggggggggggg")
                        btnConnect.setBackgroundColor(Color.GRAY)
                        listener?.onItemClick(position,devices[position])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun clearList() {
        this.devices.clear()
    }

    fun add(devices: List<BLEScanDevice>) {
        this.devices.clear()
        this.devices.addAll(devices)
        notifyDataSetChanged()
    }

    fun add(device: BLEScanDevice) {

        if (this.devices.find { it.macAddress == device.macAddress } == null) {
            this.devices.add(device)
//            notifyDataSetChanged()
        } else {
            val index: Int = this.devices.indexOfFirst { it.macAddress == device.macAddress }
            if (index != -1)
                this.devices[index] = device
            notifyItemChanged(index, device)
        }
        notifyDataSetChanged()
    }

    fun remove(device: BLEScanDevice) {

        val index: Int = this.devices.indexOfFirst { it.macAddress == device.macAddress }
//        Log.e("TAG", " REMOVE AT " + index + " " + devices.get(index))
        if (index != -1)
            this.devices.removeAt(index)
//        notifyItemChanged(index, device)
        notifyDataSetChanged()
    }


    interface Listener {
        fun onItemClick(position: Int,deviceModel: BLEScanDevice)
    }
}