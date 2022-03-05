package com.zxj.touch.multitouch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zxj.common.decodeResource
import com.zxj.common.dp
import com.zxj.touch.R
import java.util.*

/**
 * 合作性
 */
class CooperationView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val bitmap = resources.decodeResource(R.drawable.rengwuxian, 200.dp)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var offsetX = 0f
    private var offsetY = 0f

    private var downX = 0f
    private var downY = 0f

    private var originOffsetX = 0f
    private var originOffsetY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                calcFocusPoint(event).also {
                    downX = it.x
                    downY = it.y

                    originOffsetX = offsetX
                    originOffsetY = offsetY
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)
                calcFocusPoint(event, pointerId).also {
                    downX = it.x
                    downY = it.y

                    originOffsetX = offsetX
                    originOffsetY = offsetY
                }
            }

            MotionEvent.ACTION_MOVE -> {
                calcFocusPoint(event).also {
                    offsetX = it.x - downX + originOffsetX
                    offsetY = it.y - downY + originOffsetY
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }

    private fun calcFocusPoint(event: MotionEvent, excludePointerId: Int = -1): PointF {
        var count = event.pointerCount
        var sumX = 0f
        var sumY = 0f
        for (i in 0 until event.pointerCount) {
            if (event.getPointerId(i) == excludePointerId) {
                count--
                continue
            }
            sumX += event.getX(i)
            sumY += event.getY(i)
        }
        return PointF(sumX / count, sumY / count)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint)
    }

}