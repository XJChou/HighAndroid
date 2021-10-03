package com.zxj.touch.multitouch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zxj.common.decodeResource
import com.zxj.common.dp
import com.zxj.touch.R

/**
 * 接力型
 */
class RelayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val bitmap = resources.decodeResource(R.drawable.rengwuxian, 200.dp)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var offsetX = 0f
    private var offsetY = 0f

    private var lastDownId = 0
    private var lastX = 0f
    private var lastY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return onTouchEventNormal(event)
//        return onTouchEventError(event)
    }

    /**
     * 正常版本
     */
    private fun onTouchEventNormal(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                lastDownId = event.getPointerId(event.actionIndex)

                // 每次 down 和 pointerDown 的时候需要更新下 lastX
                lastX = event.getX(event.actionIndex)
                lastY = event.getY(event.actionIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.getX(event.findPointerIndex(lastDownId))
                val nowY = event.getY(event.findPointerIndex(lastDownId))
                offsetX += nowX - lastX
                offsetY += nowY - lastY
                invalidate()

                lastX = nowX
                lastY = nowY
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // 如果是当前手指需要换最后一根手指
                if (lastDownId == event.getPointerId(event.actionIndex)) {
                    lastDownId = event.getPointerId(event.pointerCount - 2)

                    lastX = event.getX(event.findPointerIndex(lastDownId))
                    lastY = event.getY(event.findPointerIndex(lastDownId))
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lastDownId = 0
            }
        }
        return true
    }

    /**
     * 异常版本，思考异常版本为什么是这样
     * 1、抢不过来 【 down的eventIndex 是 0 】
     * 2、双指情况下：松开老手指跳动问题[ 新手指从 index 1 -> 0，所以会跳动，相当于老手指直接瞬移过去 ]
     * 3、双指情况下：松开老手指，在放回老手指，跳动问题[ 老手指index重新回0,又相当于瞬移过去 ]
     */
    private fun onTouchEventError(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 每次 down 和 pointerDown 的时候需要更新下 lastX
                lastX = event.getX()
                lastY = event.getY()
            }

            MotionEvent.ACTION_MOVE -> {
                val nowX = event.getX()
                val nowY = event.getY()
                offsetX += nowX - lastX
                offsetY += nowY - lastY
                invalidate()

                lastX = nowX
                lastY = nowY
            }

            MotionEvent.ACTION_UP -> {
            }
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, offsetX, offsetY, paint)

    }
}