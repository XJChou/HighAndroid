package com.zxj.animation.view

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import com.zxj.common.dp

private val RADIUS = 20.dp

/**
 * 自定义估值器
 */
class PointView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var point = PointF(0f, 0f)
        set(value) {
            field = value
            invalidate()
        }

    init {
        val animator = ObjectAnimator.ofObject(
            this,
            "point",
            PointFEvaluator(),
            PointF(0f, 0f),
            PointF(150.dp, 300.dp),
            PointF(300.dp, 0.dp)
        )
        animator.duration = 3000L
        animator.startDelay = 1000L
//        animator.interpolator = AccelerateInterpolator()
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val point = point
        canvas.drawCircle(point.x + RADIUS, point.y + RADIUS, RADIUS, paint)
    }
}

class PointFEvaluator : TypeEvaluator<PointF> {
    override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF {
        return startValue + (endValue - startValue) * fraction
    }
}

private operator fun PointF.times(fraction: Float): PointF {
    return PointF(x * fraction, y * fraction)
}


