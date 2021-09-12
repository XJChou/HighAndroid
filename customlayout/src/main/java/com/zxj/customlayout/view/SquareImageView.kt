package com.zxj.customlayout.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/**
 * 正方形ImageView,
 * 但不符合开发者所约定的内容
 */
class SquareImageView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

}