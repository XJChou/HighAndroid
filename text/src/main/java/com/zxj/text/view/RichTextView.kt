package com.zxj.text.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zxj.common.dp
import com.zxj.text.R

const val CONTENT =
    "Integer fringilla commodo tempus. Aenean a nisi vitae sapien vulputate commodo nec sed neque. Maecenas nibh felis, dignissim non dolor nec, egestas lacinia tortor. Vestibulum maximus lacus a interdum fringilla. Duis in augue nec enim blandit consequat ut quis tellus. Etiam tincidunt diam vel augue interdum pharetra. Fusce tristique ipsum nisi, eget ullamcorper ex viverra nec. Nulla eu pharetra felis. Pellentesque pulvinar mollis elit, a faucibus orci fringilla et. Donec porttitor justo eu dolor dignissim blandit. Praesent aliquam condimentum turpis semper laoreet. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Suspendisse ut luctus lectus. In velit mauris, posuere a volutpat ac, mollis quis purus. Nullam euismod sodales metus sit amet blandit. Integer et ex semper, ullamcorper turpis sit amet, bibendum felis."

private val SIZE_TEXT = 16.dp

private val BITMAP_SIZE = 150.dp
private val BITMAP_TOP = 80.dp

class RichTextView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = SIZE_TEXT
        it.color = 0xFF666666.toInt()
    }

    private var startTextIndex = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1、绘制图片
        canvas.drawBitmap(drawAvatar(BITMAP_SIZE.toInt()), width - BITMAP_SIZE, BITMAP_TOP, paint)

        // 2、多行绘制文本
        val fontMetrics = paint.fontMetrics
        var drawTextY = -fontMetrics.top
        while (startTextIndex < CONTENT.length) {
            val validWidth = if (checkCollide(drawTextY, fontMetrics)) {
                canvas.width.toFloat() - BITMAP_SIZE
            } else {
                canvas.width.toFloat()
            }

            val writeCount = paint.breakText(
                CONTENT,
                startTextIndex, CONTENT.length,
                true, validWidth, null
            )

            canvas.drawText(
                CONTENT,
                startTextIndex,
                startTextIndex + writeCount,
                0f,
                drawTextY,
                paint
            )

            startTextIndex += writeCount
            drawTextY += paint.fontSpacing
        }
    }

    private fun checkCollide(drawTextY: Float, fontMetrics: Paint.FontMetrics): Boolean {
        // 完全大于图片bottom
        if (BITMAP_TOP >= drawTextY + fontMetrics.bottom) {
            return false
        }
        if (drawTextY + fontMetrics.top >= BITMAP_TOP + BITMAP_SIZE) {
            return false
        }
        return true
    }

    private fun drawAvatar(width: Int): Bitmap {
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, R.drawable.avatar, option)

        option.inJustDecodeBounds = false
        option.inDensity = option.outWidth
        option.inTargetDensity = width
        return BitmapFactory.decodeResource(resources, R.drawable.avatar, option)
    }
}