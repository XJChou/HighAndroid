package com.zxj.fragment.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.transition.Transition
import android.transition.TransitionValues
import android.util.Log
import android.util.Property
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView

class ChangeText : Transition() {

    init {
        addTarget(TextView::class.java)
    }

    companion object {
        private val TEXT_COLOR_PROPERTY =
            object : Property<TextView, Int>(Int::class.java, "textColor") {
                override fun set(textView: TextView, value: Int) {
                    textView.setTextColor(value)
                }

                override fun get(view: TextView): Int {
                    return view.currentTextColor
                }
            }

        private val TEXT_SIZE_PROPERTY =
            object : Property<TextView, Float>(Float::class.java, "textSize") {
                override fun set(textView: TextView, value: Float) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
                }

                override fun get(view: TextView): Float {
                    return view.textSize
                }
            }

        private const val TEXT_COLOR_NAME = "TEXT_COLOR_NAME"
        private const val TEXT_SIZE_NAME = "TEXT_SIZE_NAME"
    }

    override fun captureStartValues(values: TransitionValues) {
        captureValues(values)
    }

    override fun captureEndValues(values: TransitionValues) {
        captureValues(values)
    }

    private fun captureValues(values: TransitionValues) {
        val textView = values.view as TextView
        values.values[TEXT_COLOR_NAME] = textView.currentTextColor
        values.values[TEXT_SIZE_NAME] = textView.textSize
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues,
        endValues: TransitionValues
    ): Animator? {
        val textView = endValues.view as TextView
        textView.pivotX = 0f
        textView.pivotY = 0f

        // 颜色
        val startColor = startValues.values[TEXT_COLOR_NAME] as Int
        val endColor = endValues.values[TEXT_COLOR_NAME] as Int
        val colorAnimator = ObjectAnimator
            .ofArgb(textView, TEXT_COLOR_PROPERTY, startColor, endColor)

        // 字体大小
        val startSize = startValues.values[TEXT_SIZE_NAME] as Float
        val endSize = endValues.values[TEXT_SIZE_NAME] as Float
        val sizeAnimator = ObjectAnimator
            .ofFloat(textView, TEXT_SIZE_PROPERTY, startSize, endSize)

        Log.e(
            "ChangeText",
            "size = [${startSize} -> ${endSize}], color = [${startColor} -> ${endColor}]"
        )
        // 一直执行
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(colorAnimator, sizeAnimator)
        return animatorSet
    }


}