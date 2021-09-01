package com.zxj.animation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zxj.common.FloatInvalidate
import com.zxj.common.dp

class CircleView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var radius by FloatInvalidate(30.dp)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF008888.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }
}