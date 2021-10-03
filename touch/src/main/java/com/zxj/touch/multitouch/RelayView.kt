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
import java.util.*

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

    /**
     * 存储手指顺序
     */
    private var queue = LinkedList<Int>()

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
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    resetTouchData()
                }

                lastDownId = event.getPointerId(event.actionIndex)
                queue.addLast(lastDownId)

                // 每次 down 和 pointerDown 的时候需要更新下 lastX
                lastX = event.getX(event.actionIndex)
                lastY = event.getY(event.actionIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                val index = event.findPointerIndex(lastDownId)
                val nowX = event.getX(index)
                val nowY = event.getY(index)
                offsetX += nowX - lastX
                offsetY += nowY - lastY
                invalidate()

                lastX = nowX
                lastY = nowY
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointId = event.getPointerId(event.actionIndex)
                queue.remove(pointId)

                if (lastDownId == pointId) {
                    lastDownId = queue.peekLast()

                    val index = event.findPointerIndex(lastDownId)
                    lastX = event.getX(index)
                    lastY = event.getY(index)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                resetTouchData()
            }
        }
        return true
    }

    private fun resetTouchData() {
        lastDownId = 0
        queue.clear()
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