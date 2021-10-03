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
 * 合作性[放大和缩小]
 */
class CooperationView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val bitmap = resources.decodeResource(R.drawable.rengwuxian, 200.dp)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var offsetX = 0f
    private var offsetY = 0f

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(
            bitmap,
            width / 2f - bitmap.width / 2,
            height / 2f - bitmap.height / 2,
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

}