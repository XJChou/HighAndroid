package com.zxj.text

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zxj.text.view.RingView

class MainActivity : AppCompatActivity() {

    private lateinit var ringView: RingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ringView = findViewById(R.id.ringview)
    }

    fun switchMode(view: View) {
        ringView.switchMode()
    }
}