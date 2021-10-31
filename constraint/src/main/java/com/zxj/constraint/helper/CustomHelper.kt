package com.zxj.constraint.helper

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

private const val TAG = "updatePreLayout"

class CustomHelper(context: Context, attrs: AttributeSet) : ConstraintHelper(context, attrs) {


    /**
     * 高老师在这里改的
     *
     * 更新子类约束的时候【在实际测量子view之前】
     */
    override fun updatePreLayout(container: ConstraintLayout?) {
        super.updatePreLayout(container)
        Log.e(TAG, "updatePreLayout")
        ConstraintSet().apply {
            this.isForceId = false
            this.clone(container)
            applyTo(container)
        }
    }

    /**
     * 测量完成
     */
    override fun updatePostMeasure(container: ConstraintLayout?) {
        super.updatePostMeasure(container)
        Log.e(TAG, "updatePostMeasure")
    }

    /**
     * 布局完成
     */
    override fun updatePostLayout(container: ConstraintLayout?) {
        super.updatePostLayout(container)
        Log.e(TAG, "updatePostLayout")
    }


    /**
     * 绘制之前
     */
    override fun updatePreDraw(container: ConstraintLayout?) {
        super.updatePreDraw(container)
        Log.e(TAG, "updatePreDraw")
    }

    /**
     * 未找到调用处
     */
    override fun updatePostConstraints(constainer: ConstraintLayout) {
        super.updatePostConstraints(constainer)
        Log.e(TAG, "updatePostConstraints")
    }
}