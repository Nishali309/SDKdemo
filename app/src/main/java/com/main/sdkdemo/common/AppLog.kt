package com.main.demotoast

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*


object AppLog {

    fun e(TAG: String, msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg!!)
        }
    }

    fun i(TAG: String?, msg: String?) {
        if (BuildConfig.DEBUG) Log.i(TAG, msg!!)
    }

    fun d(TAG: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg)
        }
    }

    fun v(TAG: String?, msg: String?) {
        if (BuildConfig.DEBUG) Log.v(TAG, msg!!)
    }

    fun dataLog(TAG: String, msg: String) {
        /*if (Constants.DEBUG) {
            Log.e(TAG, msg)
            getLogObj().addDebugLog("$TAG -> $msg")
        }*/
    }

    private fun getFullDate(): String? {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS")
        val date: Date = Calendar.getInstance().getTime()
        return sdf.format(date)
    }
}