package com.zxj.materialedittext

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zxj.materialedittext.view.MaterialEditTextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_switch).setOnClickListener {
            val view = findViewById<MaterialEditTextView>(R.id.material)
            view.useFloatLabel = !view.useFloatLabel
        }
    }
}