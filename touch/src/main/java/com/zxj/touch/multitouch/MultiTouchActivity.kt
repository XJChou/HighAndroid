package com.zxj.touch.multitouch

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zxj.touch.R

class MultiTouchActivity : AppCompatActivity() {

    private lateinit var views: Array<View>
    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_touch)

        views = arrayOf(
            findViewById(R.id.view_relay),
            findViewById(R.id.view_cooperation),
            findViewById(R.id.view_self)
        )

        btn = findViewById(R.id.btn_switch)

        var show = 0
        btn.setOnClickListener { show(++show) }
        show(show)
    }

    private fun show(show: Int) {
        views.forEachIndexed { index, view ->
            view.visibility = if (show % views.size == index) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        val test = arrayOf("接力型", "合作型", "各自为战型")
        btn.text = test[show % views.size]
    }
}