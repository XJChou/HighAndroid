package com.zxj.common.transitionx

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.util.Property
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.Transition
import androidx.transition.TransitionValues
import com.zxj.common.R
import com.zxj.common.bean.SharedElementItem

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
        val snapshot = values.view.getTag(R.id.shared_element_snapshot_start)
                as? SharedElementItem
        if (snapshot == null) {
            captureValues(values)
        } else {
            captureValues(values, snapshot)
        }
    }

    override fun captureEndValues(values: TransitionValues) {
        val snapshot = values.view.getTag(R.id.shared_element_snapshot_end)
                as? SharedElementItem
        if (snapshot == null) {
            captureValues(values)
        } else {
            captureValues(values, snapshot)
        }
    }

    private fun captureValues(values: TransitionValues, item: SharedElementItem) {
        values.values[TEXT_COLOR_NAME] = item.getInt(SharedElementItem.TEXT_VIEW_COLOR)
        values.values[TEXT_SIZE_NAME] = item.getFloat(SharedElementItem.TEXT_VIEW_SIZE)
    }

    private fun captureValues(values: TransitionValues) {
        val textView = values.view as TextView
        values.values[TEXT_COLOR_NAME] = textView.currentTextColor
        values.values[TEXT_SIZE_NAME] = textView.textSize
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) return null
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