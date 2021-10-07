package com.zxj.touch.scalable

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import com.zxj.common.decodeResource
import com.zxj.touch.R

/**
 * 1、撑满宽或者高      [Finish]
 * 2、双击指定地方放大   [双击完成 + 放大完成 + 指定地方完成]
 * 3、手指交互放大和缩小 []
 * 4、图片可以拖拽 [移动完成 + fling{有卡顿}]
 * 5、边界处理   [Finish]
 * 6、缩小动态矫正
 */
class ScalableImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var bitmap: Bitmap

    /**
     * 初始位置
     */
    private var originX = 0f
    private var originY = 0f

    /**
     * 偏移位置
     */
    private var offsetX = 0f
        set(value) {
            with(getOffsetXRange()) {
                field = value
                field = when {
                    this[0] > value -> this[0]
                    this[1] < value -> this[1]
                    else -> value
                }
            }
            invalidate()
        }
    private var offsetY = 0f
        set(value) {
            with(getOffsetYRange()) {
                field = value
                field = when {
                    this[0] > value -> this[0]
                    this[1] < value -> this[1]
                    else -> value
                }
            }
            invalidate()
        }

    /**
     * 倍数控制
     */
    private var scale = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var minScale = 1f
    private var maxScale = 2f

    private val MAX_SCALE = 2f

    /**
     * 惯性滑动控制
     */
    private var mOverScroller = OverScroller(context, DecelerateInterpolator())

    /**
     * 惯性滑动操作者
     */
    private val flingRunnable = FlingRunnable()

    /**
     * 双击手势监听
     */
    private val mGestureDetector = GestureDetectorCompat(context, ScalableGestureDetector())

    /**
     * 控件宽高准备好
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        /* 1、计算图片比例 */
        val options = BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, R.drawable.rengwuxian, options)

        /* 2、计算最小缩放比，最大缩放比 */
        val ratio = options.outWidth.toFloat() / options.outHeight
        val viewRatio = width.toFloat() / height
        if (ratio >= viewRatio) {
            minScale = width / options.outWidth.toFloat()
            maxScale = height / options.outHeight.toFloat() * MAX_SCALE
        } else {
            minScale = height / options.outHeight.toFloat()
            maxScale = width / options.outWidth.toFloat() * MAX_SCALE
        }

        scale = minScale
        bitmap = resources.decodeResource(R.drawable.rengwuxian, options.outWidth)

        originX = (width - options.outWidth) / 2f
        originY = (height - options.outHeight) / 2f
    }

    /**
     * 手势监听处理
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /* 这样偏移不用考虑倍数 */
        canvas.translate(offsetX, offsetY)
        /* 图片居中放大 */
        canvas.scale(scale, scale, width / 2f, height / 2f)
        /* 在居中绘制图片 */
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    private fun getOffsetXRange(): Array<Float> {
        val bitmapWidth = bitmap.width * scale
        val viewWidth = width
        return if (viewWidth >= bitmapWidth) {
            arrayOf(0f, 0f)
        } else {
            arrayOf((viewWidth - bitmapWidth) / 2f, (bitmapWidth - viewWidth) / 2f)
        }
    }

    private fun getOffsetYRange(): Array<Float> {
        val bitmapHeight = bitmap.height * scale
        val viewHeight = height
        return if (viewHeight >= bitmapHeight) {
            arrayOf(0f, 0f)
        } else {
            arrayOf((viewHeight - bitmapHeight) / 2f, (bitmapHeight - viewHeight) / 2f)
        }
    }


    inner class ScalableGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent) = true

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ) = true.also {
            /* 用户期望是看到自己滑动与图片移动的距离是一致的，所以直接减去移动的距离即可 */
            offsetX -= distanceX
            offsetY -= distanceY
        }

        /**
         * 有顿挫感
         */
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ) = true.also {
            val xRange = getOffsetXRange()
            val yRange = getOffsetYRange()
            mOverScroller.fling(
                offsetX.toInt(),
                offsetY.toInt(),
                velocityX.toInt(),
                velocityY.toInt(),
                xRange[0].toInt(),
                xRange[1].toInt(),
                yRange[0].toInt(),
                yRange[1].toInt()
            )

            ViewCompat.postOnAnimation(this@ScalableImageView, flingRunnable)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val originScale = scale
            scale = if (scale == maxScale) {
                minScale
            } else {
                maxScale
            }

            /* 从view坐标系需要转到canvas坐标系 */
            offsetX = (width / 2f - e.x) / originScale * scale
            offsetY = (height / 2f - e.y) / originScale * scale
            return true
        }
    }


    inner class FlingRunnable : Runnable {
        override fun run() {
            if (mOverScroller.computeScrollOffset()) {
                offsetX = mOverScroller.currX.toFloat()
                offsetY = mOverScroller.currY.toFloat()
                invalidate()
                ViewCompat.postOnAnimation(this@ScalableImageView, this)
            }
        }
    }
}