package com.zxj.touch.scalable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.zxj.common.decodeResource
import com.zxj.touch.R

/**
 * 1、撑满宽或者高      [Finish]
 * 2、双击指定地方放大   [双击完成 + 放大完成 + ]
 * 3、手指交互放大和缩小 []
 * 4、图片可以拖拽 [移动完成]
 * 5、边界处理
 */
class ScalableImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var bitmap: Bitmap

    private var originX = 0f
    private var originY = 0f

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
    private var scale = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var minScale = 1f
    private var maxScale = 2f

    /* 手势 */
    private val mGestureDetector = GestureDetectorCompat(context, ScalableGestureDetector())

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }

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
            maxScale = height / options.outHeight.toFloat()
        } else {
            minScale = height / options.outHeight.toFloat()
            maxScale = width / options.outWidth.toFloat()
        }

        scale = minScale
        bitmap = resources.decodeResource(R.drawable.rengwuxian, options.outWidth)

        originX = (width - options.outWidth) / 2f
        originY = (height - options.outHeight) / 2f
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(originX + bitmap.width / 2f, originY + bitmap.height / 2f)
        canvas.scale(scale, scale)
        canvas.translate(-originX - bitmap.width / 2f, -originY - bitmap.height / 2f)
        canvas.drawBitmap(bitmap, originX + offsetX, originY + offsetY, paint)
    }

    inner class ScalableGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            /* 因为位移会放大scale倍，所以这里要先除一下 */
            offsetX -= distanceX / scale
            offsetY -= distanceY / scale
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Toast.makeText(context, "onFling", Toast.LENGTH_SHORT).show()
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            scale = if (scale == maxScale) {
                minScale
            } else {
                maxScale
            }
            return true
        }
    }
}