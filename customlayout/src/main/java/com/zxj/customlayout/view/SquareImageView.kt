package com.zxj.customlayout.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/**
 * 正方形ImageView
 */
class SquareImageView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs) {

    /**
     * 可以让父view感知
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }
//
//    /**
//     * 父view不可感知
//     */
//    override fun layout(l: Int, t: Int, r: Int, b: Int) {
//        val width = r - l
//        val height = b - t
//        val size = min(width, height)
//        super.layout(l, t, l + size, t + size)
//    }

}