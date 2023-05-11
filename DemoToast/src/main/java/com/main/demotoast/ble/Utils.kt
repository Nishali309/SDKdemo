package com.urbitdemo.main.ble

//import okhttp3.internal.and
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.experimental.and

object Utils {
    private var HEX_ARRAY = "0123456789ABCDEF".toCharArray()
    private val HEX = "0123456789ABCDEF".toCharArray()
    const val SERVICE_SCAN_UUID = "7BE300C21F6983A8EC4316D357ACC033"
    const val CHARACTERISTIC_UUID = "AEC5E807-83E9-4FCE-A5A9-3790CD63A977"
    const val SERVICE_UUID = "33C0AC57-D316-43EC-A883-691FC200E37B"
    var ACTION_GATT_SERVICE_DISCOVERED = "com.urbitdemo.ACTION_GATT_SERVICE_DISCOVERED"
    var ACTION_GATT_DISCONNECTED = "com.urbitdemo.ACTION_GATT_DISCONNECTED"
    var ACTION_IOS_DEVICE_NAME = "com.urbitdemo.ACTION_IOS_DEVICE_NAME"
    var ACTION_DESCRIPTER_WRITE = "com.urbitdemo.ACTION_DESCRIPTER_WRITE"
    var ACTION_CONNECT_FAIL = "com.urbitdemo.ACTION_CONNECT_FAIL"

    fun byteToHex(byte: Byte): String {
        val bytes = byteArrayOf(byte)
        if (bytes == null || bytes.size == 0) {
            return ""
        }
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = ((bytes[j] and 0xFF.toByte()).toInt())
            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    fun byteToString(byte: ByteArray): String {
        /*val bytes = byteArrayOf(byte)
        if (bytes == null || bytes.size == 0) {
            return ""
        }*/
        val str = String(byte, StandardCharsets.UTF_8);

        return str
    }

    fun bytesToHex(bytes: ByteArray?): String {
        if (bytes == null || bytes.size == 0) {
            return ""
        }
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = (bytes[j] and 0xFF.toByte()).toInt()
            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }

    fun toHexString(bytes: ByteArray): String {
        if (bytes.isEmpty()) {
            return ""
        }
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = (bytes[j].toInt() and 0xFF)
            hexChars[j * 2] = HEX[v ushr 4]
            hexChars[j * 2 + 1] = HEX[v and 0x0F]
        }
        return String(hexChars)
    }

    fun reverse(str: String?): String {
        return StringBuilder(str).reverse().toString()
    }

    fun sha256(str: String): ByteArray {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        return digest.digest(str.toByteArray(StandardCharsets.UTF_8))
    }

    fun hexToAscii(hex: String): String {

        val output = StringBuilder().apply {
            var i = 0
            while (i < hex.length) {
                val str: String = hex.substring(i, i + 2)
                append(str.toInt(16).toChar())
                i += 2
            }
        }

        return output.toString()
    }

    fun parseBE2BytesAsInt(data: ByteArray, offset: Int): Int {
        return (data[offset + 0].toInt() and 0xFF shl 8
                or (data[offset + 1].toInt() and 0xFF shl 0))
    }

    fun hexToDecimal(data: String): Int {
        return data.toInt(16)
    }

    fun reverse(array: ByteArray?): ByteArray? {
        if (null == array) {
            return null
        }
        var i = 0
        var j = array.size - 1
        var tmp: Byte
        while (j > i) {
            tmp = array[j]
            array[j] = array[i]
            array[i] = tmp
            j--
            i++
        }
        return array
    }

}