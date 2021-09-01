package com.zxj.animation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zxj.animation.fragment.FirstFragment
import com.zxj.animation.fragment.FourthFragment
import com.zxj.animation.fragment.SecondFragment
import com.zxj.animation.fragment.ThirdFragment

class MainActivity : AppCompatActivity() {

    private val index = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bt = supportFragmentManager.beginTransaction()
        when (index) {
            /* 题目一：属性动画 */
            1 -> {
                bt.replace(R.id.fl_content, FirstFragment.newInstance())
            }

            /* 题目二：自定义估值动画[PointF] */
            2 -> {
                bt.replace(R.id.fl_content, SecondFragment.newInstance())
            }

            /* 题目三：多属性动画 */
            3 -> {
                bt.replace(R.id.fl_content, ThirdFragment.newInstance())
            }

            /* 题目三：自定义估值动画[String] */
            4 -> {
                bt.replace(R.id.fl_content, FourthFragment.newInstance())
            }
        }
        bt.commit()
    }

}