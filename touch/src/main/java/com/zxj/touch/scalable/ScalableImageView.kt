package com.zxj.touch.scalable

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
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
 * 3、手指交互放大和缩小 [功能完成，但交互有bug]
 * 4、图片可以拖拽 [移动完成 + fling完成]
 * 5、边界处理   [Finish]
 * 6、缩小动态矫正 [FINISH]
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
            field = value
            invalidate()
        }
    private var offsetY = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f

    /**
     * 倍数控制
     */
    private var scale = 0f
        set(value) {
            field = value
            invalidate()
        }
    private val scaleAnimator by lazy {
        ObjectAnimator.ofFloat(0f, 1f).also {
            it.addUpdateListener {
                scale = startScale + (endScale - startScale) * (it.animatedValue as Float)
                offsetX = startX + (endX - startX) * (it.animatedValue as Float)
                offsetY = startY + (endY - startY) * (it.animatedValue as Float)
            }
        }
    }

    private var startScale = 0f
    private var endScale = 0f

    private var minScale = 1f
    private var maxScale = 2f
    private val MAX_SCALE_FRACTION = 2f

    /**
     * 惯性滑动控制
     * Scroller是没有初始速度的[设置也没有用]
     * OverScroller可以设置初始速度且OverFling
     */
    private var overScroller = OverScroller(context, DecelerateInterpolator())

    /**
     * 惯性滑动操作者
     */
    private val flingRunnable = FlingRunnable()

    /**
     * 双击手势监听
     */
    private val gestureDetector = GestureDetectorCompat(context, ZGestureDetector())

    private val scaleGestureDetector = ScaleGestureDetector(context, ZScaleGestureDetector())

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
            maxScale = height / options.outHeight.toFloat() * MAX_SCALE_FRACTION
        } else {
            minScale = height / options.outHeight.toFloat()
            maxScale = width / options.outWidth.toFloat() * MAX_SCALE_FRACTION
        }

        scale = minScale
        bitmap = resources.decodeResource(R.drawable.rengwuxian, options.outWidth)

        originX = (width - options.outWidth) / 2f
        originY = (height - options.outHeight) / 2f
    }

    private var isScale = false

    /**
     * 手势监听处理
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            overScroller.abortAnimation()
            isScale = false
        }
        scaleGestureDetector.onTouchEvent(event)

        /* 如果开始双指放大和缩小，则双击手势不执行 */
        if (scaleGestureDetector.isInProgress || isScale) {
            isScale = true
        } else {
            gestureDetector.onTouchEvent(event)
        }

        if (event.actionMasked == MotionEvent.ACTION_UP) {
            isScale = false
        }
        return true
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /* [*]用户手势控制 */
        canvas.translate(offsetX, offsetY)

        /* 图片居中放大 */
        canvas.scale(scale, scale, width / 2f, height / 2f)

        /* 在居中绘制图片 */
        canvas.drawBitmap(bitmap, originX, originY, paint)
    }

    /**
     * 获取横向可滑动区域
     */
    private fun getOffsetXRange(scale: Float = this.scale): Array<Float> {
        val bitmapWidth = bitmap.width * scale
        val viewWidth = width
        return if (viewWidth >= bitmapWidth) {
            arrayOf(0f, 0f)
        } else {
            arrayOf((viewWidth - bitmapWidth) / 2f, (bitmapWidth - viewWidth) / 2f)
        }
    }

    /**
     * 获取纵向可滑动区域
     */
    private fun getOffsetYRange(scale: Float = this.scale): Array<Float> {
        val bitmapHeight = bitmap.height * scale
        val viewHeight = height
        return if (viewHeight >= bitmapHeight) {
            arrayOf(0f, 0f)
        } else {
            arrayOf((viewHeight - bitmapHeight) / 2f, (bitmapHeight - viewHeight) / 2f)
        }
    }

    private fun checkOffsetX(offsetX: Float, scale: Float = this.scale): Float {
        val xRange = getOffsetXRange(scale)
        return offsetX.coerceAtMost(xRange[1]).coerceAtLeast(xRange[0])
    }

    private fun checkOffsetY(offsetY: Float, scale: Float = this.scale): Float {
        val yRange = getOffsetYRange(scale)
        return offsetY.coerceAtMost(yRange[1]).coerceAtLeast(yRange[0])
    }


    /**
     * 手势控制器
     */
    inner class ZGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent) = true

        override fun onScroll(
            downEvent: MotionEvent,
            currentEvent: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ) = true.also {
            /* 用户期望是看到自己滑动与图片移动的距离是一致的，所以直接减去移动的距离即可 */
            offsetX = checkOffsetX(offsetX - distanceX)
            offsetY = checkOffsetY(offsetY - distanceY)
        }

        /**
         * 有顿挫感
         */
        override fun onFling(
            downEvent: MotionEvent,
            currentEvent: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ) = true.also {
            val xRange = getOffsetXRange()
            val yRange = getOffsetYRange()
            overScroller.fling(
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
            // 双击出发的时候，终止计算
            overScroller.abortAnimation()

            val originScale = scale
            val targetScale = if (scale == minScale) {
                maxScale
            } else {
                minScale
            }

            // 点哪 - 哪放大版
            val targetX =
                checkOffsetX((targetScale / originScale - 1) * (width / 2f - e.x), targetScale)
            val targetY =
                checkOffsetY((targetScale / originScale - 1) * (height / 2f - e.y), targetScale)
            // 点击的移动到屏幕正方
//            val targetX = (offsetX + (width / 2 - e.x) / originScale * targetScale)
//                .coerceAtMost(xRange[1]).coerceAtLeast(xRange[0])
//            val targetY = (offsetY + (height / 2 - e.y) / originScale * targetScale)
//                .coerceAtMost(yRange[1]).coerceAtLeast(yRange[0])


            startScale = originScale
            endScale = targetScale

            startX = offsetX
            startY = offsetY
            endX = targetX
            endY = targetY

            scaleAnimator.start()
            return true
        }
    }


    inner class ZScaleGestureDetector : ScaleGestureDetector.OnScaleGestureListener {
        var originScale = 0f
        var originOffsetX = 0f
        var originOffsetY = 0f

        var focusX = 0f
        var focusY = 0f
        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            val targetScale = scale * detector.scaleFactor
//            if (targetScale in minScale..maxScale) {
//                offsetX = checkOffsetX(
//                    originOffsetX - (targetScale / originScale - 1) * focusX,
//                    targetScale
//                )
//                offsetY = checkOffsetY(
//                    originOffsetY - (targetScale / originScale - 1) * focusY,
//                    targetScale
//                )
//
//                scale = targetScale
//                return true
//            }
//            // [*] 返回值是有意义的
//            // true 是 scaleFactor 是 返回 当前状态 和 上一个状态 比值
//            // false 是 scaleFactor 返回 当前状态 和 上一个状态 比值
//            return false

            // 修改原因：防止scaleFactor过大导致没有达到限制就不缩放了
            val targetScale = (originScale *  detector.scaleFactor)
                .coerceAtLeast(minScale)
                .coerceAtMost(maxScale)
            offsetX = checkOffsetX(
                originOffsetX - (targetScale / originScale - 1) * focusX,
                targetScale
            )
            offsetY = checkOffsetY(
                originOffsetY - (targetScale / originScale - 1) * focusY,
                targetScale
            )
            scale = targetScale
            return false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            focusX = detector.focusX - width / 2f
            focusY = detector.focusY - height / 2f

            originScale = scale
            originOffsetX = offsetX
            originOffsetY = offsetY
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }
    }


    /**
     * 惯性滑动
     */
    inner class FlingRunnable : Runnable {
        override fun run() {
            if (overScroller.computeScrollOffset()) {
                offsetX = overScroller.currX.toFloat()
                offsetY = overScroller.currY.toFloat()
                invalidate()
                ViewCompat.postOnAnimation(this@ScalableImageView, this)
            }
        }
    }
}