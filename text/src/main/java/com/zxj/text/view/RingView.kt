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
            canvas.width / 2 - RADIUS,
            canvas.height / 2 - RADIUS,
            canvas.width / 2 + RADIUS,
            canvas.height / 2 + RADIUS,
            -90f, 235f, false, paint
        )

        // 3、绘制文本[严格居中]
        val text = "abap"
        paint.style = Paint.Style.FILL
        paint.textSize = SIZE_TEXT
        paint.textAlign = Paint.Align.CENTER
        paint.getTextBounds(text, 0, text.length, bounds)
        canvas.drawText(
            text,
            canvas.width / 2f,
            canvas.height / 2f - (bounds.top + bounds.bottom) / 2f,
            paint
        )

        // 4、绘制方格
        paint.strokeWidth = 1.dp
        paint.style = Paint.Style.STROKE
        bounds.let {
            var halfWidth = bounds.width() / 2
            var halfHeight = bounds.height() / 2
            canvas.drawRect(
                canvas.width / 2f - halfWidth, canvas.height / 2f - halfHeight,
                canvas.width / 2f + halfWidth, canvas.height / 2f + halfHeight,
                paint
            )
        }

        // 4、绘制文本[不严格居中]
//        val text = "abab"
//        paint.style = Paint.Style.FILL
//        paint.textSize = SIZE_TEXT
//        paint.textAlign = Paint.Align.CENTER
//        val fontMetrics = paint.fontMetrics
//        canvas.drawText(
//            text,
//            canvas.width / 2f,
//            canvas.height / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f,
//            paint
//        )
//
//        paint.strokeWidth = 1.dp
//        paint.style = Paint.Style.STROKE
//        bounds.let {
//            var halfWidth = paint.measureText(text) / 2f
//            var halfHeight = (fontMetrics.descent - fontMetrics.ascent) / 2f
//            canvas.drawRect(
//                canvas.width / 2f - halfWidth, canvas.height / 2f - halfHeight,
//                canvas.width / 2f + halfWidth, canvas.height / 2f + halfHeight,
//                paint
//            )
//        }
    }

}