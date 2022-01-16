package com.zxj.butterknife

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zxj.lib.SimpleButterKnife
import com.zxj.lib_annotation.BindView

class MainActivity : AppCompatActivity() {

    @BindView(R.id.textView)
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SimpleButterKnife.bind(this)
    }
}