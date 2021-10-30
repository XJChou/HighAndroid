package com.zxj.constraint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.zxj.constraint.databinding.ActivityMainBinding

/**
 * 常用约束布局属性：
 * 1、layout_constraintBaseline_toBaselineOf
 * 2、layout_constraintHorizontal_bias | layout_constraintVertical_bias
 * 3、layout_constrainedWidth | layout_constrainedHeight
 * 4、layout_goneMarginStart
 * 5、layout_constraintDimensionRatio
 * 6、layout_constraintVertical_weight | layout_constraintHorizontal_weight
 *
 * 常用的Helper
 * 1、Barrier
 * 2、Flow
 * 3、Group
 * 4、Layer
 * 5、custom - ConstraintHelper
 *
 * 黑科技：
 * Placeholder
 * ConstraintLayoutStates
 *
 * 约束位置相关：
 * ConstraintSet
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initDemo1()
    }

    fun initDemo1() {
        var index = 0
        binding.includeDemo1.tvBig.setOnClickListener {
            index = 0
        }
        binding.includeDemo1.tvSmall.setOnClickListener {
            index = 1
        }
        binding.includeDemo1.btnAdd.setOnClickListener {
            if (index == 0) {
                binding.includeDemo1.tvBig.text =
                    binding.includeDemo1.tvBig.text.toString() + "Z"
            } else {
                binding.includeDemo1.tvSmall.text =
                    binding.includeDemo1.tvSmall.text.toString() + "z"
            }
        }
        binding.includeDemo1.btnMinus.setOnClickListener {
            if (index == 0) {
                val value = binding.includeDemo1.tvBig.text.toString()
                binding.includeDemo1.tvBig.text =
                    value.subSequence(0, (value.length - 1).coerceAtLeast(0))
            } else {
                val value = binding.includeDemo1.tvSmall.text.toString()
                binding.includeDemo1.tvSmall.text =
                    value.subSequence(0, (value.length - 1).coerceAtLeast(0))
            }
        }
    }


}