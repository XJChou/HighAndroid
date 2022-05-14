package com.zxj.customlayout.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.zxj.common.dp
import kotlin.random.Random

/**
 * 1. startNestedScroll
 * 2. dispatchNestedPreScroll
 */
class NestScrollChildView(context: Context?, attrs: AttributeSet?) : View(context, attrs),
    NestedScrollingChild3 {

    private val childHelper = NestedScrollingChildHelper(this).also {
        it.isNestedScrollingEnabled = true
    }

    private var lastX = 0f
    private var lastY = 0f

    private val text by lazy { buildRandomText() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = 16f.dp
        it.color = 0xFF000000.toInt()
    }
    private var textHeight = 0f

    // 每一行有 文本 + 起始高度
    private val textLines = arrayListOf<String>()
    private val textTops = arrayListOf<Float>()


    private fun buildRandomText(): String {
        val len = 2_000
        val sb = StringBuilder("start-")
        (0 until len).forEach {
            sb.append(('a' + Random.nextInt(26)))
        }
        sb.append("-end")
        return sb.toString()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        textLines.clear()
        textTops.clear()
        textHeight = calcTextHeight(text)
    }

    private fun calcTextHeight(text: String): Float {
        val lineHeight = textPaint.fontMetrics.let { it.bottom - it.top }

        var s = 0
        var height = 0f
        while (s < text.length) {
            val useChar = textPaint
                .breakText(text, s, text.length, true, width.toFloat(), null)
            textLines.add(text.substring(s, s + useChar))
            textTops.add(height)

            s += useChar
            height += lineHeight
        }
        return height
    }

    private var offsetY = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val consume = intArrayOf(0, 0)

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.getX(0)
                lastY = event.getY(0)
                childHelper.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
            }
            MotionEvent.ACTION_MOVE -> {
                var deltaX = (event.getX(0) - lastX + 0.5f)
                var deltaY = (event.getY(0) - lastY + 0.5f)

                // 先问父布局是否消耗
                if (dispatchNestedPreScroll(deltaX.toInt(), deltaY.toInt(), consume, null)) {
                    deltaX -= consume[0]
                    deltaY -= consume[1]
                }

                // 自己消耗
                val originOffsetY = offsetY
                offsetY = (originOffsetY + deltaY)
                    .coerceAtMost(0f)
                    .coerceAtLeast(-textHeight + height)

                val unConsumeX = deltaX
                val consumeY = offsetY - originOffsetY
                dispatchNestedScroll(
                    0, consumeY.toInt(),
                    unConsumeX.toInt(), (consumeY - deltaY).toInt(),
                    null, ViewCompat.TYPE_TOUCH
                )
                lastX = event.getX(0)
                lastY = event.getY(0)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                childHelper.stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
        }
        return true
    }

    /**
     * 优化：
     *
     * 根据当前位置能够定位到哪一行，减少onDraw的无用输出[先测量好每一行的位置即可]
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val originOffsetY = -textPaint.fontMetrics.top
        val lineHeight = textPaint.fontMetrics.let { it.bottom - it.top }
        val s = getLineIndex(offsetY)
        val e = getLineIndex(offsetY - height - lineHeight)

        (s until e).forEach {
            canvas.drawText(textLines[it], 0f, textTops[it] + offsetY + originOffsetY, textPaint)
        }
    }

    private fun getLineIndex(offset: Float): Int {
        val lineHeight = textPaint.fontMetrics.let { it.bottom - it.top }
        textTops.forEachIndexed { index, item ->
            if (item + offset + lineHeight > 0) return index
        }
        return textTops.size - 1
    }


    /**
     * 委托方法
     */
    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return childHelper.startNestedScroll(axes, type)
    }

    /**
     * 纯委托
     */
    override fun stopNestedScroll(type: Int) {
        childHelper.stopNestedScroll(type)
    }

    /**
     * 纯委托
     */
    override fun hasNestedScrollingParent(type: Int): Boolean {
        return childHelper.hasNestedScrollingParent(type)
    }

    /**
     * 通过ChildHelper分发给父view, 滚动先前置问下父类
     */
    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    /**
     * 通过ChildHelper分发给父view
     */
    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        childHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow,
            type,
            consumed
        )
    }


}