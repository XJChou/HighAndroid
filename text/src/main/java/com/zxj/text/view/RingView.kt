package com.zxj.text.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.zxj.common.dp

const val COLOR_SELECT = 0xff90a4ae.toInt()
const val COLOR_UNSELECT = 0xFFFF4081.toInt()
private val RING_WIDTH = 20.dp
private val RADIUS = 150.dp

private val SIZE_TEXT = 100.dp

class RingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = Rect()
    private var mode = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1、绘制背景圆环
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = RING_WIDTH
        paint.color = COLOR_SELECT
        canvas.drawCircle(width / 2f, height / 2f, RADIUS, paint)

        // 2、绘制选中区域
        paint.color = COLOR_UNSELECT
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(
            width / 2 - RADIUS,
            height / 2 - RADIUS,
            width / 2 + RADIUS,
            height / 2 + RADIUS,
            -90f, 235f, false, paint
        )

        // 3、绘制中心线
        paint.strokeWidth = 2f
        canvas.drawLine(0f, height / 2f - 1, width.toFloat(), height / 2f + 2, paint)

        when (mode) {
            0 -> {
                // 3、绘制文本[严格居中]
                drawStrictText(canvas)
            }
            else -> {
                // 4、绘制文本[不严格居中]
                drawLineText(canvas)
            }
        }
    }

    private fun drawStrictText(canvas: Canvas) {
        val text = "abap"
        paint.style = Paint.Style.FILL
        paint.textSize = SIZE_TEXT
        paint.textAlign = Paint.Align.CENTER
        paint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            width / 2f,
            height / 2f - (bounds.top + bounds.bottom) / 2f,
            paint
        )

        // 4、绘制方格
        paint.strokeWidth = 1.dp
        paint.style = Paint.Style.STROKE
        bounds.let {
            val halfWidth = bounds.width() / 2
            val halfHeight = bounds.height() / 2
            canvas.drawRect(
                width / 2f - halfWidth, height / 2f - halfHeight,
                width / 2f + halfWidth, height / 2f + halfHeight,
                paint
            )
        }
    }

    private fun drawLineText(canvas: Canvas) {
        val text = "abap"
        paint.style = Paint.Style.FILL
        paint.textSize = SIZE_TEXT
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics

        // ************************************
//        val ascentOffset = height / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2 +
//                fontMetrics.ascent
//        canvas.drawLine(0.toFloat(), ascentOffset, width.toFloat(), ascentOffset, paint)

//        val baselineOffset = height / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2
//        canvas.drawLine(0.toFloat(), baselineOffset, width.toFloat(), baselineOffset, paint)
//
//        val topOffset = height / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2 +
//                fontMetrics.top
//        canvas.drawLine(0.toFloat(), topOffset, width.toFloat(), topOffset, paint)
//
//        val bottomOffset = height / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2 +
//                fontMetrics.bottom
//        canvas.drawLine(0.toFloat(), bottomOffset, width.toFloat(), bottomOffset, paint)
////
//        val descentOffset = height / 2 - (fontMetrics.ascent + fontMetrics.descent) / 2 +
//                fontMetrics.descent
//        canvas.drawLine(0.toFloat(), descentOffset, width.toFloat(), descentOffset, paint)

        // ************************************


        canvas.drawText(
            text,
            width / 2f,
            height / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f,
            paint
        )

        paint.strokeWidth = 1.dp
        paint.style = Paint.Style.STROKE
        bounds.let {
            val halfWidth = paint.measureText(text) / 2f
            val halfHeight = (fontMetrics.descent - fontMetrics.ascent) / 2f
            canvas.drawRect(
                width / 2f - halfWidth, height / 2f - halfHeight,
                width / 2f + halfWidth, height / 2f + halfHeight,
                paint
            )
        }
    }

    fun switchMode() {
        mode = (mode + 1) % 2
        invalidate()
    }
}