package com.zxj.touch.multitouch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import androidx.core.util.forEach
import androidx.core.util.set
import com.zxj.common.dp

/**
 * 各自为战型
 */
class SelfView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // pointerId -> Paint  MotionEvent {x, y, id, index} id是不变的，index是跟随数目变化
    private val sparseArray = SparseArray<Path>()

    private val paint = Paint().also {
        it.color = Color.BLACK
        it.style = Paint.Style.STROKE
        it.strokeWidth = 3.dp
    }

    /**
     * 触摸事件完成 action_index 只有 pointer_down 和 pointer_up 才能得到，其余都是 0
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                /* 只有pointerDown 或者 pointerUp才有actionIndex，但down和up巧好为0 */
                val pointerId = event.getPointerId(event.actionIndex)
                val x = event.getX(event.actionIndex)
                val y = event.getY(event.actionIndex)
                sparseArray[pointerId] = Path().also {
                    it.moveTo(x, y)
                }
                invalidate()
            }


            MotionEvent.ACTION_MOVE -> {
                /* 因为不知道那个手指操作，所以全部都遍历一次 单次会话对应的手指id是一样的 */
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    val x = event.getX(i)
                    val y = event.getY(i)
                    sparseArray[pointerId].lineTo(x, y)
                }
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.getPointerId(event.actionIndex)
                sparseArray.remove(pointerId)
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {
                sparseArray.removeAtRange(0, sparseArray.size())
                invalidate()
            }
        }
        return true
    }

    /**
     * 绘制
     */
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        sparseArray.forEach { key, value ->
            canvas.drawPath(value, paint)
        }
    }
}