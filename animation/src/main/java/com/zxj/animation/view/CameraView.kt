package com.zxj.animation.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import com.zxj.animation.R
import com.zxj.common.FloatInvalidate
import com.zxj.common.decodeResource
import com.zxj.common.dp
import com.zxj.common.withSave

class CameraView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val camera = Camera().also {
        it.setLocation(0f, 0f, -6.dp)
    }
    private val BITMAP_SIZE = 200.dp
    private val bitmap = resources.decodeResource(R.drawable.rengwuxian, BITMAP_SIZE)

    init {
        val topFlipAnimator = ObjectAnimator
            .ofFloat(this, "topFlip", -45f)

        val bottomFlipAnimator = ObjectAnimator
            .ofFloat(this, "bottomFlip", 45f)

        val rotateFlipAnimator = ObjectAnimator
            .ofFloat(this, "rotateFlip", 270f)


        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(bottomFlipAnimator, rotateFlipAnimator, topFlipAnimator)
        animatorSet.startDelay = 1000L
        animatorSet.duration = 2000L
        animatorSet.start()

    }

    var topFlip by FloatInvalidate(0f)
    var bottomFlip by FloatInvalidate(0f)
    var rotateFlip by FloatInvalidate(0f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 上半部分
        canvas.withSave {
            canvas.translate(width / 2f, height / 2f)
            rotate(-rotateFlip)
            camera.withSave {
                rotateX(topFlip)
                applyToCanvas(canvas)
            }
            canvas.clipRect(-BITMAP_SIZE, -BITMAP_SIZE, BITMAP_SIZE, 0f)
            canvas.rotate(rotateFlip)
            canvas.translate(-width / 2f, -height / 2f)
            canvas.drawBitmap(
                bitmap,
                width / 2f - BITMAP_SIZE / 2,
                height / 2f - BITMAP_SIZE / 2f,
                paint
            )
        }

        // 下半部分
        canvas.withSave {
            canvas.translate(width / 2f, height / 2f)
            canvas.rotate(-rotateFlip)
            camera.withSave {
                rotateX(bottomFlip)
                applyToCanvas(canvas)
            }
            canvas.clipRect(-BITMAP_SIZE, 0f, BITMAP_SIZE, BITMAP_SIZE)
            canvas.rotate(rotateFlip)
            canvas.translate(-width / 2f, -height / 2f)
            canvas.drawBitmap(
                bitmap,
                width / 2f - BITMAP_SIZE / 2,
                height / 2f - BITMAP_SIZE / 2f,
                paint
            )
        }
    }
}