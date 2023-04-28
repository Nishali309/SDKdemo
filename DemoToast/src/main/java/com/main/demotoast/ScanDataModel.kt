package com.main.demotoast

import java.io.Serializable
import kotlin.collections.ArrayList

class ScanDataModel : Serializable{
    var rssi : Int? = 0
    var advertiseData: ByteArray? = null
    var serviceData: ArrayList<String>? = null
    var manufactureData: ArrayList<ByteArray>? = null
    var serviceUuids: ArrayList<String>? = null
    var id: String =String()
    var name: String = String()
    var isConnectable: Boolean? = false
    override fun toString(): String {
        return "ScanDataModel(rssi=$rssi, advertiseData=${advertiseData?.contentToString()}, serviceData=$serviceData, manufactureData=$manufactureData, serviceUuids=$serviceUuids, id='$id', name='$name', isConnectable=$isConnectable)"
    }

}