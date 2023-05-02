package com.main.demotoast.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable


fun EditText.isNotEmpty(showMsg: Boolean = true): Boolean {
    return if (this.text.isNotEmpty()) {
        true
    } else {
        if (showMsg)
            Toast.makeText(
                this.context,
                "Please enter",
                Toast.LENGTH_SHORT
            ).show()
        false
    }
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        @SuppressLint("SetTextI18n")
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun BluetoothDevice.removeBond() {
    try {
        javaClass.getMethod("removeBond").invoke(this)
    } catch (e: Exception) {
        Log.e(this.address, "Removing bond has been failed. ${e.message}")
    }
}

fun Context.sendBroadcast(action: String = "BleStatusUpdate", key: String = "data", data: Any?) {
    val intent = Intent(action)
    Log.e("Sending Broadcast...", "$action --- $key --- $data")
    when (data) {
        is String? -> intent.putExtra(key, data)
        is ArrayList<*> -> intent.putExtra(key, data)
        is HashMap<*,*> -> intent.putExtra(key, data)
        is Long -> intent.putExtra(key, data)
        is LongArray -> intent.putExtra(key, data)
        is Short -> intent.putExtra(key, data)
        is ShortArray -> intent.putExtra(key, data)
        is Char -> intent.putExtra(key, data)
        is CharSequence -> intent.putExtra(key, data)
        is CharArray -> intent.putExtra(key, data)
        is Bundle -> intent.putExtra(key, data)
        is Int -> intent.putExtra(key, data)
        is IntArray -> intent.putExtra(key, data)
        is Double -> intent.putExtra(key, data)
        is Float -> intent.putExtra(key, data)
        is DoubleArray -> intent.putExtra(key, data)
        is FloatArray -> intent.putExtra(key, data)
        is ByteArray -> intent.putExtra(key, data)
        is Byte -> intent.putExtra(key, data)
        is Serializable -> intent.putExtra(key, data)
        is Parcelable -> intent.putExtra(key, data)
    }
    this.sendBroadcast(intent)
}


// OS versions
fun ifAtLeast(version: Int, f: () -> Unit) {
    if (android.os.Build.VERSION.SDK_INT >= version) f()
}

fun atLeast(version: Int): Boolean = android.os.Build.VERSION.SDK_INT >= version

fun Fragment.isGranted(permission: String) = run {
    context?.let {
        (PermissionChecker.checkSelfPermission(
            it, permission
        ) == PermissionChecker.PERMISSION_GRANTED)
    } ?: false
}

//fun Fragment.shouldShowRationale(permission: AppPermission) = run {
//    shouldShowRequestPermissionRationale(permission.permissionName)
//}
//
//fun Fragment.requestPermission(permission: AppPermission) {
//    requestPermissions(arrayOf(permission.permissionName), permission.requestCode
//    )
//}
//
fun AppCompatActivity.checkPermission(permission: String) = run {
    this.let {
        (ActivityCompat.checkSelfPermission(
            it, permission
        ) == PermissionChecker.PERMISSION_GRANTED)
    } ?: false
}
//
//fun AppCompatActivity.shouldRequestPermissionRationale(permission: AppPermission) =
//    ActivityCompat.shouldShowRequestPermissionRationale(this, permission.permissionName)
//
//fun AppCompatActivity.requestAllPermissions(permission: AppPermission) {
//    ActivityCompat.requestPermissions(this, arrayOf(permission.permissionName), permission.requestCode))
//}

fun withDelay(delay: Long, block: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(Runnable(block), delay)
}

fun <T> List<T>.removeConcurrent(data: Any) {
    val itr: MutableIterator<T> = this.toMutableList().iterator()
    while (itr.hasNext()) {
        val t = itr.next()
        if (t == data) {
            itr.remove()
        }
    }
}

private suspend fun CoroutineScope.executeBody(block: suspend CoroutineScope.() -> Unit) {
    try {
        block.invoke(this)
    } catch (e: Exception) {
        e.printStackTrace();
        Log.e("executeBody", " ${e.message}")
    }
}

suspend fun AppCompatActivity.mainScopeSuspended(block: suspend CoroutineScope.() -> Unit) =
    withContext(Dispatchers.Main) {
        executeBody(block)
    }

suspend fun Fragment.mainScopeSuspended(block: suspend CoroutineScope.() -> Unit) =
    withContext(Dispatchers.Main) { executeBody(block) }


suspend inline fun <T, R> T.onMain(crossinline block: (T) -> R): R =
    withContext(mainDispatcher) { this@onMain.let(block) }

suspend inline fun <T> onMain(crossinline block: CoroutineScope.() -> T): T =
    withContext(mainDispatcher) { block.invoke(this@withContext) }


suspend inline fun <T> onDefault(crossinline block: CoroutineScope.() -> T): T =
    withContext(defaultDispatcher) { block.invoke(this@withContext) }

suspend inline fun <T, R> T.onDefault(crossinline block: (T) -> R): R =
    withContext(defaultDispatcher) { this@onDefault.let(block) }


suspend inline fun <T, R> T.onIO(crossinline block: (T) -> R): R =
    withContext(ioDispatcher) { this@onIO.let(block) }

suspend inline fun <T> onIO(crossinline block: CoroutineScope.() -> T): T =
    withContext(ioDispatcher) { block.invoke(this@withContext) }


val mainDispatcher = Dispatchers.Main
val defaultDispatcher = Dispatchers.Default
val ioDispatcher = Dispatchers.IO