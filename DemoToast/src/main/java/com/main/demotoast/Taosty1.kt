package com.main.demotoast

import android.content.Context
import android.widget.Toast

public class Taosty1 {

    companion object {
        fun print1(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

}