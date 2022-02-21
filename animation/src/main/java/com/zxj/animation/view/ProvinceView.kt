package com.zxj.animation.view

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.zxj.common.dp

val PROVINCES = arrayOf(
    "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省",
    "江苏省", "浙江省", "安徽省", "福建省", "江西省",
    "山东省", "河南省", "湖北省", "湖南省", "广东省",
    "海南省", "四川省", "贵州省", "云南省", "陕西省",
    "甘肃省", "青海省", "台湾省"
)

class ProvinceView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var province = PROVINCES[0]
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.textAlign = Paint.Align.CENTER
        it.textSize = 100.dp
        it.color = 0xFF333333.toInt()
    }

    init {
        val animator = ObjectAnimator.ofObject(this, "province", StringEvaluator(), "台湾省")
        animator.startDelay = 1000L
        animator.duration = 5000L
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val fm = paint.fontMetrics
        canvas.drawText(province, width / 2f, height / 2f - (fm.top + fm.bottom) / 2f, paint)
    }

}

class StringEvaluator : TypeEvaluator<String> {

    override fun evaluate(fraction: Float, startValue: String, endValue: String): String {
        val start = PROVINCES.indexOf(startValue)
        val end = PROVINCES.indexOf(endValue)
        return PROVINCES[(start + (end - start) * fraction).toInt()]
    }
}