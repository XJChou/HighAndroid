package com.zxj.customlayout.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import kotlin.math.max

class TabLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    /**
     * 测量子view和自身的宽高
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var measuredWidth = 0
        var measuredHeight = 0

//        ViewCompat.startDragAndDrop(null, clipData, shadowBuilder, localState, flags)
//        DragShadowBuilder(this)
        var start = 0

        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val layoutParams = child.layoutParams

            /* 1、测量子view的宽度和高度 */
            child.measure(
                getChildMeasured(widthMeasureSpec, layoutParams.width),
                getChildMeasured(heightMeasureSpec, layoutParams.height)
            )
            start += child.measuredWidth

            /* 2、模拟布局 */
            measuredWidth = max(measuredWidth, start)
            measuredHeight = max(measuredHeight, child.measuredHeight)
        }

        /* 3、得出算出整体的宽高 */
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    private fun getChildMeasured(measureSpec: Int, devRequest: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)

        var childMode = 0
        var childSize = 0
        when (devRequest) {
            LayoutParams.FILL_PARENT, LayoutParams.MATCH_PARENT -> {
                when (mode) {
                    MeasureSpec.AT_MOST -> {
                        /* MeasureSpec.AT_MOST 也可以【取决于算法】 */
                        childMode = MeasureSpec.EXACTLY
                        childSize = size
                    }
                    MeasureSpec.EXACTLY -> {
                        childMode = MeasureSpec.EXACTLY
                        childSize = size
                    }
                    MeasureSpec.UNSPECIFIED -> {
                        childMode = MeasureSpec.UNSPECIFIED
                        childSize = 0
                    }
                }
            }
            LayoutParams.WRAP_CONTENT -> {
                when (mode) {
                    MeasureSpec.AT_MOST -> {
                        childMode = MeasureSpec.AT_MOST
                        childSize = size
                    }
                    MeasureSpec.EXACTLY -> {
                        childMode = MeasureSpec.AT_MOST
                        childSize = size
                    }
                    MeasureSpec.UNSPECIFIED -> {
                        childMode = MeasureSpec.UNSPECIFIED
                        childSize = 0
                    }
                }
            }
            /* 固定值 */
            else -> {
                childMode = MeasureSpec.EXACTLY
                childSize = devRequest
            }
        }
        return MeasureSpec.makeMeasureSpec(childSize, childMode)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        /* 布局子控件 */
        var start = l
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.layout(start, t, start + child.measuredWidth, t + child.measuredHeight)
            start += child.measuredWidth
        }
    }


    /**
     * 生成子view的LayoutParams
     */
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return super.generateLayoutParams(attrs)
    }

    /**
     * 标识当前ViewGroup不可滚动
     */
    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

}