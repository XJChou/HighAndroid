package com.zxj.common.transition

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.Transition
import androidx.transition.TransitionSet
import androidx.transition.TransitionValues

class TextChangeTransition() : TransitionSet() {

    init {
        ordering = ORDERING_TOGETHER
        addTransition(ChangeTextSize())
        addTransition(ChangeTextColor())
    }
}

class ChangeTextSize : Transition() {

    companion object {
        val PROP_TEXT_SIZE = "PROP_TEXT_SIZE"
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        if (view !is TextView) return
        transitionValues.values[PROP_TEXT_SIZE] = view.textSize
    }


    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val view = endValues?.view ?: return null
        if (view !is TextView) return null
        val startMap = startValues?.values ?: return null
        val endMap = endValues?.values ?: return null

        val startColor = startMap[PROP_TEXT_SIZE] as Float
        val endColor = endMap[PROP_TEXT_SIZE] as Float
        return ValueAnimator.ofFloat(startColor, endColor).also {
            val updateListener = ValueAnimator.AnimatorUpdateListener {
                (view as TextView).setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    it.animatedValue as Float
                )
            }
            it.addUpdateListener(updateListener)
        }
    }
}

class ChangeTextColor : Transition() {

    companion object {
        val PROP_TEXT_COLOR = "PROP_TEXT_COLOR"
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    private fun captureValues(transitionValues: TransitionValues) {
        val view = transitionValues.view
        if (view !is TextView) return
        transitionValues.values[PROP_TEXT_COLOR] = view.currentTextColor
    }


    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val view = endValues?.view ?: return null
        if (view !is TextView) return null

        val startMap = startValues?.values ?: return null
        val endMap = endValues?.values ?: return null

        val startColor = startMap[PROP_TEXT_COLOR] as Int
        val endColor = endMap[PROP_TEXT_COLOR] as Int
        return ObjectAnimator.ofArgb(view as TextView, "textColor", startColor, endColor)
    }
}