package com.zxj.fragment.gridtodetail.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zxj.common.commit
import com.zxj.fragment.R
import com.zxj.fragment.databinding.ActivityGridPageBinding

class GridPageActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGridPageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        supportFragmentManager.commit {
            add(R.id.fl_content, GridFragment.newInstance())
        }
    }
}