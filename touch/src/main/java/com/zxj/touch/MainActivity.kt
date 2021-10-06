package com.zxj.touch

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zxj.touch.multitouch.MultiTouchActivity
import com.zxj.touch.scalable.ScalableActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, ScalableActivity::class.java))
        finish()
    }
}