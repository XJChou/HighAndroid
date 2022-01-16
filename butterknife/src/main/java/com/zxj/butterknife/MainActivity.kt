package com.zxj.butterknife

import android.graphics.Color.CYAN
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zxj.lib.SimpleButterKnife
import com.zxj.lib_annotation.BindView

class MainActivity : AppCompatActivity() {

    @BindView(R.id.textView)
    lateinit var textView: TextView

    @BindView(R.id.root)
    lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 反射调用生成的Binding文件
        SimpleButterKnife.bind(this)

        textView.text = "binding success"
        root.setBackgroundColor(CYAN)
    }
}