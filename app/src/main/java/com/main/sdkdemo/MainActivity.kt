package com.main.sdkdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.main.demotoast.Taosty1

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Taosty1.print1(this@MainActivity,"HELLO HELOOO HELLOOO....")
    }
}