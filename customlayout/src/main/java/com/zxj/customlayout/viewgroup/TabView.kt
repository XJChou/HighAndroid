package com.zxj.customlayout.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.zxj.common.dp
import kotlin.random.Random

/* padding */
private val PADDING_HORIZONTAL = 10.dp.toInt()
private val PADDING_VERTICAL = 5.dp.toInt()
private val COLOR_TEXT = arrayOf(
    0xFF9C661F,
    0xFFE3170D,
    0xFFFF7F50,
    0xFF8B4513,
    0xFFDDA0DD,

    0xFF8A2BE2,
    0xFFA020F0,
    0xFF228B22,
    0xFF7CFC00,
    0xFFFF4500
)

/**
 * 自定义tab样式
 * 字体大小随机
 * 背景颜色随机
 * padding固定
 * 圆角背景
 */
class TabView(context: Context) : View(context) {
    /* 字体大小 */
    private val fontSize = Random.nextInt(15, 35).dp
    private val fontColor = Color.WHITE

    var text: String = ""
        set(value) {
            field = value
            requestLayout()
        }

    /* 背景随机 */
    private val backgroundColor: Int = COLOR_TEXT[Random.nextInt(0, 10)].toInt()
//        get() {
////            var color: Int
////            while (Random.nextInt().also { color = it } > 0xFF666666) {
////            }
////            return color
//            return COLOR_TEXT[Random.nextInt(0, 10)].toInt()
//        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.textSize = fontSize
        paint.color = fontColor
    }

    /**
     * 自定义测量
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fm = paint.fontMetrics
        val textWidth = paint.measureText(text)
        val textHeight = fm.bottom - fm.top

        val minWidth = 2 * PADDING_HORIZONTAL + textWidth
        val minHeight = 2 * PADDING_VERTICAL + textHeight

        val measureWidth = resolveSize(minWidth.toInt(), widthMeasureSpec)
        val measureHeight = resolveSize(minHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measureWidth, measureHeight)
    }

    /**
     * 自定义绘制
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        /* 1、绘制背景 */
        paint.style = Paint.Style.FILL
        paint.color = backgroundColor
        canvas.drawRoundRect(
            0F, 0F,
            canvas.width.toFloat(), canvas.height.toFloat(),
            5.dp, 5.dp,
            paint
        )

        /* 2、绘制文字 */
        paint.color = fontColor
        /* 本来是baseline位置，需要下移top位置 */
        canvas.drawText(
            text,
            PADDING_HORIZONTAL.toFloat(),
            PADDING_VERTICAL - paint.fontMetrics.top,
            paint
        )

    }


}