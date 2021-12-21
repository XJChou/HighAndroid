package com.zxj.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zxj.common.commit
import com.zxj.fragment.fragment.ListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.commit {
            add(R.id.fl_content, ListFragment())
        }
    }
}