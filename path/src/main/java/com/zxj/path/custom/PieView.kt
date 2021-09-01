package com.zxj.customviewdrawing.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


private val TRANSLATION = 20f.px

class PieView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    /**
     * 角度
     */
    private val ARRAY_ANGLE = arrayOf(80f, 100f, 120f, 60f)


    /**
     * 颜色值
     */
    private val ARRAY_COLOR = arrayOf(
        0xff881133.toInt(), 0xff331188.toInt(), 0xff118833.toInt(), 0xffff1188.toInt()
    )

    /**
     * 半径
     */
    private val RADIUS = 150f.px

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var selectIndex = -1

    init {
        // radius
        // randian
        paint.strokeWidth = 0f
        paint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        onDrawPie(canvas)
    }

    private fun onDrawPie(canvas: Canvas) {
        var startAngle = 0f
        for ((index, item) in ARRAY_ANGLE.withIndex()) {
            paint.color = ARRAY_COLOR[index]

            if (selectIndex == index) {
                var radians = Math.toRadians((startAngle + item / 2).toDouble()).toFloat()
                canvas.save()
                canvas.translate(TRANSLATION * cos(radians), TRANSLATION * sin(radians))
            }

            canvas.drawArc(
                width / 2f - RADIUS, height / 2f - RADIUS,
                width / 2f + RADIUS, height / 2f + RADIUS,
                startAngle, item, true, paint
            )

            if (selectIndex == index) {
                canvas.restore()
            }

            startAngle += item
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val deltaX = event.x - width / 2f
                val deltaY = event.y - height / 2f
                val radians = Math.atan2(deltaY.toDouble(), deltaX.toDouble())
                var angle = Math.toDegrees(radians)
                while(angle < 0) {
                    angle += 360
                }
                var startAngle = 0f
                for ((index, item) in ARRAY_ANGLE.withIndex()) {
                    Log.e("PieView", "angle = ${angle}, [${startAngle.toDouble()} -> ${(startAngle + item)}]")
                    if (angle in startAngle..(startAngle + item)) {
                        selectIndex = index
                        postInvalidate()
                        return true
                    }
                    startAngle += item
                }
            }
        }
        return super.onTouchEvent(event)
    }
}