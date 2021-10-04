package com.zxj.customlayout

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class LayoutViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_view)

        val view = findViewById<View>(R.id.view)
        val circle = findViewById<View>(R.id.circle)
        val siv = findViewById<View>(R.id.siv)

        siv.setOnClickListener {
            siv.visibility = View.GONE
            view.visibility = View.GONE
            circle.visibility = View.VISIBLE
        }
        circle.setOnClickListener {
            siv.visibility = View.VISIBLE
            view.visibility = View.VISIBLE
            circle.visibility = View.GONE
        }
    }
}