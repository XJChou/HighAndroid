package com.zxj.customviewdrawing.custom

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

const val OPEN_ANGLE = 120f
val DASH_WIDTH = 2f.px
val DASH_HEIGHT = 10f.px
val POINTER_RADIUS = 120f.px

class DashboardView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val dashPath = Path()
    private val arcPath = Path()

    private var pathEffect = PathEffect()
    private val pathMeasure = PathMeasure()

    private var index = 0f
    private var radian: Float = Math.toRadians((90 + OPEN_ANGLE / 2).toDouble()).toFloat()
    private var isTouch = false


    init {
        dashPath.addRect(0f, 0f, DASH_WIDTH, DASH_HEIGHT, Path.Direction.CW)

        paint.strokeWidth = 3f.px
        paint.style = Paint.Style.STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        arcPath.reset()

        val halfWidth = width / 2f
        val halfHeight = height / 2f
        arcPath.addArc(
            halfWidth - 150f.px,
            halfHeight - 150f.px,
            halfWidth + 150f.px,
            halfHeight + 150f.px,
            90f + OPEN_ANGLE / 2f,
            360f - OPEN_ANGLE
        )

        pathMeasure.setPath(arcPath, false)
        val arcLength = pathMeasure.length

        pathEffect = PathDashPathEffect(
            dashPath,
            (arcLength - DASH_WIDTH) / 20,
            0f,
            PathDashPathEffect.Style.MORPH
        )
//        pathEffect = PathDashPathEffect(
//            dashPath,
//            arcLength / 20f,
//            DASH_WIDTH,
//            PathDashPathEffect.Style.MORPH
//        )

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        this.onDrawDashboard(canvas)
        this.onDrawPointer(canvas)
    }

    private fun onDrawDashboard(canvas: Canvas) {
        paint.pathEffect = pathEffect
        canvas.drawPath(arcPath, paint)
        paint.pathEffect = null

        canvas.drawPath(arcPath, paint)
    }

    private fun onDrawPointer(canvas: Canvas) {
//        val nowAngle = 90 + OPEN_ANGLE / 2 + (360.0 - OPEN_ANGLE) * index / 20
//        canvas.drawLine(
//            width / 2f, height / 2f,
//            width / 2f + POINTER_RADIUS * cos(Math.toRadians(nowAngle)).toFloat(),
//            height / 2f + POINTER_RADIUS * sin(Math.toRadians(nowAngle)).toFloat(),
//            paint
//        )
        canvas.drawLine(
            width / 2f, height / 2f,
            width / 2f + POINTER_RADIUS * cos(radian),
            height / 2f + POINTER_RADIUS * sin(radian),
            paint
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                /* 判断是否在刻度盘里面 */
                isTouch = checkDashBoardInner(event.x, event.y)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouch) {
                    val deltaX = (event.x - width / 2)
                    val deltaY = (event.y - height / 2)
                    val radian = Math.atan2(deltaY.toDouble(), deltaX.toDouble()).toFloat()
                    val s = Math.toRadians((90 - OPEN_ANGLE / 2).toDouble())
                    val e = Math.toRadians((90 + OPEN_ANGLE / 2).toDouble())
                    if (radian !in s..e) {
                        this.radian = Math.atan2(deltaY.toDouble(), deltaX.toDouble()).toFloat()
                        postInvalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isTouch) {
                    isTouch = false
                }
            }
        }
        return isTouch
    }

    private fun checkDashBoardInner(x: Float, y: Float): Boolean {
        // 先判断是否在圆内
        if (x !in (width / 2 - 150f.px)..(width / 2 + 150f.px)) {
            Log.e("DashboardView", "no in circlr x")
            return false
        }
        if (y !in (height / 2 - 150f.px)..(height / 2 + 150f.px)) {
            Log.e("DashboardView", "no in circlr y")
            return false
        }
        Log.e("DashboardView", "inner")
        return true
    }
}

val Float.px
    get():Float {
        var resource = Resources.getSystem()
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resource.displayMetrics
        )
    }