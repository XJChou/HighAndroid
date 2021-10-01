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

//    private fun calcMeasureSpec(devRequest: Int, measureSpec: Int, viewSize: Float): Int {
//        val measureSize = MeasureSpec.getSize(measureSpec)
//        val measureMode = MeasureSpec.getMode(measureSpec)
//        when (devRequest) {
//            /* 自适应 */
//            ViewGroup.LayoutParams.WRAP_CONTENT -> {
//                return when (measureMode) {
//                    MeasureSpec.EXACTLY -> {
//                        // 开发者指定该view自适应 | 父view要求 [准确值是measureSize]
//                        min(measureSize, viewSize.toInt())
//                    }
//                    MeasureSpec.AT_MOST -> {
//                        min(measureSize, viewSize.toInt())
//                    }
//                    /* MeasureSpec.UNSPECIFIED */
//                    else -> {
//                        viewSize.toInt()
//                    }
//                }
//            }
//
//            /* 铺满 */
//            ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT -> {
//                return when (measureMode) {
//                    MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
//                        measureSize
//                    }
//                    /* MeasureSpec.UNSPECIFIED */
//                    else -> {
//                        0
//                    }
//                }
//            }
//
//            /* 固定值 */
//            else -> {
//                return devRequest
////                return when (measureMode) {
////                    MeasureSpec.AT_MOST -> {
////                        devRequest
////                    }
////                    MeasureSpec.EXACTLY -> {
////                        devRequest
////                    }
////                    else -> {
////                        devRequest
////                    }
////                }
//            }
//        }
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(PADDING + RADIUS, PADDING + RADIUS, RADIUS, paint)
    }

}

//MeasureSpec.EXACTLY -> {
//    size
//}
//MeasureSpec.AT_MOST -> {
//    min(needWidth, size)
//}
///* MeasureSpec.UNSPECIFIED 没有限制则取自己想要的大小 */
//else -> {
//    size
//}