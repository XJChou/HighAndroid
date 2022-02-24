package com.zxj.customlayout.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zxj.common.dp
import kotlin.math.min

private val RADIUS = 100.dp
private val PADDING = 100.dp

/**
 * 自定义尺寸测量
 */
class CircleView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 开发者 + 父view的限制了[layoutparams是需要父view算子view才需要的]
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /* view自身最小宽度 */
        val needWidth = ((PADDING + RADIUS) * 2).toInt()
        val needHeight = ((PADDING + RADIUS) * 2).toInt()

        /* 计算宽高 resolveSize[系统实现] == calcSize */
        /* 计算宽高 resolveSizeAndState[带状态]，但一般不用 */
//        resolveSize()
//        resolveSizeAndState() 这个取决于父布局，但是google原生布局都没使用这个标记，所以一般使用不限制的形式去测量子view
        val measuredWidth = calcSize(widthMeasureSpec, needWidth)
        val measuredHeight = calcSize(heightMeasureSpec, needHeight)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    /**
     * 计算子view的大小
     */
    private fun calcSize(measureSpec: Int, needSize: Int): Int {
        /* 开发者 + 父布局 给的尺寸 */
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> {
                /* 指定要求固定尺寸 */
                size
            }
            MeasureSpec.AT_MOST -> {
                /* 最大不能超过size，则 size 和 needWidth 取最小 */
                min(size, needSize)
            }
            else -> {
                /* MeasureSpec.UNSPECIFIED: 随意发挥 */
                needSize
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(PADDING + RADIUS, PADDING + RADIUS, RADIUS, paint)
    }
}
