package com.zxj.materialedittext.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.zxj.common.dp

class MaterialEditTextView(context: Context, attrs: AttributeSet) :
    AppCompatEditText(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = 16.dp
        it.color = 0xFF333333.toInt()
    }
    private val TEXT_TOP = 20.dp
    private val TEXT_LEFT = 10.dp

    init {
        setPadding(paddingTop, (paddingTop + 30.dp).toInt(), paddingRight, paddingBottom)
    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val hint = hint?.toString() ?: ""
        val fm = paint.fontMetrics
        canvas.drawText(hint, TEXT_LEFT, TEXT_TOP - (fm.top + fm.bottom) / 2, paint)
    }

}