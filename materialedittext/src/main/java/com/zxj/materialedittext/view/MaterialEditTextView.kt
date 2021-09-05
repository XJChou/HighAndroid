package com.zxj.materialedittext.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.zxj.common.FloatInvalidate
import com.zxj.common.dp
import com.zxj.materialedittext.R

class MaterialEditTextView(context: Context, attrs: AttributeSet) :
    AppCompatEditText(context, attrs) {

    private val TEXT_SIZE = 12.dp
    private val TEXT_TOP = 20.dp

    var floatFraction by FloatInvalidate(0f)
    private var isFloatLabel = false

    var useFloatLabel = true
        set(value) {
            if (value) {
                setPadding(
                    paddingLeft,
                    (paddingTop + TEXT_SIZE + TEXT_TOP).toInt(),
                    paddingRight,
                    paddingBottom
                )
            } else if (field) {
                setPadding(
                    paddingLeft,
                    (paddingTop - TEXT_SIZE - TEXT_TOP).toInt(), paddingRight, paddingBottom
                )
            }

            field = value
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textSize = TEXT_SIZE
        it.color = 0xFF333333.toInt()
    }

    /**
     * tips: 一个animator的好处，在于反转的时候是从当前开始而不是从起始点开始
     */
    private val animator by lazy {
        ObjectAnimator.ofFloat(this, "floatFraction", 0f, 1f)
    }

    init {
        // 常规调用
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditTextView)
        val useFloatLabel =
            typedArray.getBoolean(R.styleable.MaterialEditTextView_useFloatingLabel, false)
        typedArray.recycle()

        // 原理调用
//        val typedArray = context.obtainStyledAttributes(
//            attrs,
//            intArrayOf(R.attr.useFloatingLabel)
//        )
//        val useFloatLabel = typedArray.getBoolean(0, false)
//        typedArray.recycle()

        this.useFloatLabel = useFloatLabel
    }


    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (isFloatLabel && text.isNullOrEmpty()) {
            isFloatLabel = false
            animator.reverse()
        } else if (!isFloatLabel && !text.isNullOrEmpty()) {
            isFloatLabel = true
            animator.start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (useFloatLabel) {
            val fm = paint.fontMetrics
            val textOffset = (fm.bottom + fm.top) / 2f
            paint.alpha = (0xFF * floatFraction).toInt()

            var offsetY = TEXT_TOP - textOffset + (textSize + TEXT_SIZE) * (1 - floatFraction)
            canvas.drawText(hint?.toString() ?: "", paddingLeft.toFloat(), offsetY, paint)
        }
    }

}

