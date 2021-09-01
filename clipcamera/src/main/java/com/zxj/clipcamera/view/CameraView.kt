package com.zxj.clipcamera.view

import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withSave
import com.zxj.clipcamera.R
import com.zxj.common.decodeResource
import com.zxj.common.dp

private val BITMAP_SIZE = 200.dp
private val BITMAP_TOP = 100.dp

private val degree = 30f

class CameraView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val camera = Camera()

    private val bitmap = resources.decodeResource(R.drawable.rengwuxian, BITMAP_SIZE.toInt())

    init {
        camera.rotateX(30f)
        camera.setLocation(0f, 0f, (-6).dp)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        onDrawTeacher(canvas)
//        onDrawMine(canvas)
    }

    private fun onDrawTeacher(canvas: Canvas) {
        /* 上部分 */
        canvas.withSave {
            canvas.translate(BITMAP_TOP + BITMAP_SIZE / 2f, BITMAP_TOP + BITMAP_SIZE / 2f)
            canvas.rotate(-degree)
            canvas.clipRect(-BITMAP_SIZE, -BITMAP_SIZE, BITMAP_SIZE, 0f)
            canvas.rotate(degree)
            canvas.translate(-BITMAP_TOP - BITMAP_SIZE / 2f, -BITMAP_TOP - BITMAP_SIZE / 2f)
            canvas.drawBitmap(bitmap, BITMAP_TOP, BITMAP_TOP, paint)
        }

        /*下部分*/
        canvas.withSave {
            canvas.translate(BITMAP_TOP + BITMAP_SIZE / 2f, BITMAP_TOP + BITMAP_SIZE / 2f)
            canvas.rotate(-degree)
            camera.applyToCanvas(canvas)
            canvas.clipRect(-BITMAP_SIZE, 0f, BITMAP_SIZE, BITMAP_SIZE)
            canvas.rotate(degree)
            canvas.translate(-BITMAP_TOP - BITMAP_SIZE / 2f, -BITMAP_TOP - BITMAP_SIZE / 2f)
            canvas.drawBitmap(bitmap, BITMAP_TOP, BITMAP_TOP, paint)
        }

        /* 折线 */
        paint.color = 0xff000000.toInt()
        paint.style = Paint.Style.FILL
        canvas.withSave {
            canvas.translate(width / 2f, BITMAP_TOP + BITMAP_SIZE / 2)
            canvas.rotate(-degree)
            canvas.translate(-width / 2f, -BITMAP_TOP - BITMAP_SIZE / 2)
            canvas.drawRect(
                0f,
                BITMAP_TOP + BITMAP_SIZE / 2 - 1.dp,
                width.toFloat(),
                BITMAP_TOP + BITMAP_SIZE / 2 + 1.dp,
                paint
            )
//            canvas.drawLine(
//                0f,
//                BITMAP_TOP + BITMAP_SIZE / 2 - 1.dp / 2,
//                width.toFloat(),
//                BITMAP_TOP + BITMAP_SIZE / 2 - 1.dp / 2,
//                paint
//            )
        }
    }

    private fun onDrawMine(canvas: Canvas) {
        // 上部分
        canvas.withSave {
            canvas.translate(width / 2f, height / 2f)
            canvas.rotate(-30f)
            canvas.clipRect(-BITMAP_SIZE, -BITMAP_SIZE, BITMAP_SIZE, 0f)
            // 3、旋转
            canvas.rotate(30f)
            // 2、平移
            canvas.translate(-width / 2f, -height / 2f)
            // 1、绘图
            canvas.drawBitmap(
                bitmap,
                (width - BITMAP_SIZE) / 2f,
                (height - BITMAP_SIZE) / 2f,
                paint
            )
        }

        canvas.withSave {
            canvas.translate(width / 2f, height / 2f)
            canvas.rotate(-30f)
            camera.applyToCanvas(canvas)
            canvas.clipRect(
                -BITMAP_SIZE / 2 * 2, 0f,
                BITMAP_SIZE / 2 * 2, BITMAP_SIZE
            )
            canvas.rotate(30f)
            canvas.translate(-width / 2f, -height / 2f)
            canvas.drawBitmap(
                bitmap,
                (width - BITMAP_SIZE) / 2f,
                (height - BITMAP_SIZE) / 2f,
                paint
            )
        }
    }

}