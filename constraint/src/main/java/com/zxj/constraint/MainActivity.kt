package com.zxj.constraint

import android.animation.ObjectAnimator
import android.os.Bundle
import android.transition.TransitionManager
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.set
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
 * Placeholder
 *
 * 约束位置相关：
 * ConstraintSet
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val explanationDescSparse = SparseArray<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        explanationDescSparse[R.id.iv_sun] =
            "太阳(Sun)是太阳系的中心天体，占有太阳系总体质量的99.86%。太阳系中的八大行星、小行星、流星、彗星、外海王星天体以及星际尘埃等，都围绕着太阳公转，而太阳则围绕着银河系的中心公转。"
        explanationDescSparse[R.id.iv_earth] =
            "地球（英文名：Earth；拉丁文：Terra）是距离太阳的第三颗行星，也是已知的唯一孕育和支持生命的天体。地球表面的大约 29.2% 是由大陆和岛屿组成的陆地。剩余的 70.8% 被水覆盖，大部分被海洋、海湾和其他咸水体覆盖，也被湖泊、河流和其他淡水覆盖，它们共同构成了水圈。地球的大部分极地地区都被冰覆盖。地球外层分为几个刚性构造板块它们在数百万年的时间里在地表迁移，而其内部仍然保持活跃，有一个固体铁内核、一个产生地球磁场的液体外核，以及一个驱动板块构造的对流地幔。"
        explanationDescSparse[R.id.iv_moon] =
            "月球（Moon）是围绕地球旋转的球形天体，同时也是地球的天然卫星。在汉语中被俗称为月或月亮，古时又称为太阴、玄兔、婵娟、玉盘。"

        initView()
    }

    private fun initView() {
        binding.ivInvisible.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root)
            if (binding.flowGuess.visibility == View.VISIBLE) {
                binding.flowGuess.visibility = View.GONE
                (it as ImageView).setImageResource(R.drawable.ic_visible)
            } else {
                binding.flowGuess.visibility = View.VISIBLE
                (it as ImageView).setImageResource(R.drawable.ic_invisible)
            }
        }

        val animator =  ObjectAnimator.ofFloat(binding.layerExplanation,"rotation",360f).also {
            it.duration = 3000L
        }
        binding.tvExplanation.setOnClickListener {
            animator.start()
        }

        binding.ivSun.setOnClickListener { showPlaceHolderView(it) }
        binding.ivEarth.setOnClickListener { showPlaceHolderView(it) }
        binding.ivMoon.setOnClickListener { showPlaceHolderView(it) }

        showPlaceHolderView(binding.ivSun)
    }

    private fun showPlaceHolderView(view: View) {
        TransitionManager.beginDelayedTransition(binding.root)
        binding.placeholderExplanation.setContentId(view.id)
        binding.tvExplanationDesc.text = explanationDescSparse[view.id]
    }
}