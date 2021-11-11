package com.zxj.motionlayout

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.zxj.common.transition.TextChangeTransition
import com.zxj.motionlayout.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.tvTest.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root, TextChangeTransition())
            binding.tvTest.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            binding.tvTest.setTextColor(0xff888833.toInt())
        }

    }
}