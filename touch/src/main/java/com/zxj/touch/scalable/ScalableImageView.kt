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
 * 2、双击指定地方放大   [双击完成]
 * 3、手指交互放大和缩小 []
 * 4、当图片过大可以缩放
 */
class ScalableImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var bitmap: Bitmap

    private var offsetX = 0f
    private var offsetY = 0f

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

        /* 计算图片比例 */
        val options = BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, R.drawable.rengwuxian, options)

        /* 设置图片撑满一边 */
        val ratio = options.outWidth.toFloat() / options.outHeight
        val viewRatio = width.toFloat() / height
        if (ratio >= viewRatio) {
            /* 宽撑满 */
            offsetX = 0f
            offsetY = (height - width / ratio) / 2f

            bitmap = resources.decodeResource(R.drawable.rengwuxian, width)
        } else {
            /* 高撑满 */
            offsetX = (width - ratio * height) / 2f
            offsetY = 0f

            bitmap = resources.decodeResource(R.drawable.rengwuxian, ratio * height)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /* 2、点击指定位置方法 */
        /* 1、绘制图片 */
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint)
    }

    inner class ScalableGestureDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            Toast.makeText(context, "双击", Toast.LENGTH_SHORT).show()
            return super.onDoubleTap(e)
        }
    }
}