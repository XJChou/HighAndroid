package com.zxj.fragment.transition

import android.animation.Animator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.transition.Transition
import android.transition.TransitionValues
import android.util.Pair
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class ChangeOnlineImageTransition : Transition() {
    private val PROPNAME_SCALE_TYPE = "hw:changeImageTransform:scaletype"
    private val PROPNAME_BOUNDS = "hw:changeImageTransform:bounds"
    private val PROPNAME_MATRIX = "hw:changeImageTransform:matrix"

    init {
        addTarget(ImageView::class.java)
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureBoundsAndInfo(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureBoundsAndInfo(transitionValues)
    }

    private fun captureBoundsAndInfo(transitionValues: TransitionValues) {
        val view = transitionValues.view
        if (view !is ImageView || view.getVisibility() != View.VISIBLE || isFrescoView(view)) {
            return
        }
        val imageView = view
        val drawable = imageView.drawable ?: return
        val values = transitionValues.values
        val left = view.getLeft()
        val top = view.getTop()
        val right = view.getRight()
        val bottom = view.getBottom()
        val bounds = Rect(left, top, right, bottom)
        values[PROPNAME_BOUNDS] = bounds
        values[PROPNAME_SCALE_TYPE] = imageView.scaleType
        if (imageView.scaleType == ImageView.ScaleType.MATRIX) {
            values[PROPNAME_MATRIX] = imageView.imageMatrix
        }
    }

    protected fun calculateMatrix(
        startValues: TransitionValues?,
        endValues: TransitionValues?,
        imageWidth: Int,
        imageHeight: Int,
        startMatrix: Matrix?,
        endMatrix: Matrix?
    ) {
        if (startValues == null || endValues == null || startMatrix == null || endMatrix == null) {
            return
        }
        val startBounds = startValues.values[PROPNAME_BOUNDS] as Rect?
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect?
        val startScaleType = startValues.values[PROPNAME_SCALE_TYPE] as ImageView.ScaleType?
        val endScaleType = endValues.values[PROPNAME_SCALE_TYPE] as ImageView.ScaleType?
        if (startScaleType == ImageView.ScaleType.MATRIX) {
            startMatrix.set(startValues.values[PROPNAME_MATRIX] as Matrix?)
        } else {
            startMatrix.set(
                getImageViewMatrix(
                    startBounds,
                    startScaleType,
                    imageWidth,
                    imageHeight
                )
            )
        }
        if (endScaleType == ImageView.ScaleType.MATRIX) {
            endMatrix.set(endValues.values[PROPNAME_MATRIX] as Matrix?)
        } else {
            //这里要计算的是如何给出的ImageView模拟出结束状态的ImageView
            endMatrix.set(getImageViewMatrix(endBounds, endScaleType, imageWidth, imageHeight))
        }
    }

    override fun createAnimator(
        sceneRoot: ViewGroup?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null || endValues.view !is ImageView) {
            return null
        }
        val startBounds = startValues.values[PROPNAME_BOUNDS] as Rect?
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect?
        val imageView = endValues.view as ImageView
        if (startBounds == null || endBounds == null) {
            return null
        }
        return if (startBounds == endBounds) {
            null
        } else createMatrixAnimator(imageView, startValues, endValues)
    }

    private fun createMatrixAnimator(
        imageView: ImageView,
        startValues: TransitionValues,
        endValues: TransitionValues
    ): ValueAnimator? {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        val matrixArray = SparseArray<Pair<Matrix, Matrix>>(2)
        val evaluator = MatrixEvaluator()
        val scaleType = imageView.scaleType
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { animation -> //没有内容，无需做矩阵动画
            if (imageView.drawable == null) {
                return@AnimatorUpdateListener
            }
            if (imageView.drawable !is BitmapDrawable) {
                return@AnimatorUpdateListener
            }
            val drawable = imageView.drawable as BitmapDrawable
            if (drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
                return@AnimatorUpdateListener
            }
            //是否已经计算过了
            val key = drawable.hashCode()
            var matrixPair = matrixArray[key]
            if (matrixPair == null) {
                //计算对应的变化矩阵
                val startMatrix = Matrix()
                val endMatrix = Matrix()
                calculateMatrix(
                    startValues,
                    endValues,
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    startMatrix,
                    endMatrix
                )
                matrixPair = Pair(startMatrix, endMatrix)
                matrixArray.put(key, matrixPair)
            }
            //计算中间矩阵
            val imageMatrix =
                evaluator.evaluate(animation.animatedFraction, matrixPair.first, matrixPair.second)
            imageView.scaleType = ImageView.ScaleType.MATRIX
            imageView.imageMatrix = imageMatrix
        })
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                imageView.scaleType = ImageView.ScaleType.MATRIX
            }

            override fun onAnimationEnd(animation: Animator) {
                imageView.scaleType = scaleType
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        return animator
    }

    class MatrixEvaluator : TypeEvaluator<Matrix> {
        var mTempStartValues = FloatArray(9)
        var mTempEndValues = FloatArray(9)
        var mTempMatrix = Matrix()
        override fun evaluate(fraction: Float, startValue: Matrix, endValue: Matrix): Matrix {
            startValue.getValues(mTempStartValues)
            endValue.getValues(mTempEndValues)
            for (i in 0..8) {
                val diff = mTempEndValues[i] - mTempStartValues[i]
                mTempEndValues[i] = mTempStartValues[i] + fraction * diff
            }
            mTempMatrix.setValues(mTempEndValues)
            return mTempMatrix
        }
    }

    private fun getImageViewMatrix(
        bounds: Rect?,
        scaleType: ImageView.ScaleType?,
        contentWidth: Int,
        contentHeight: Int
    ): Matrix? {
        val matrix = Matrix()
        val vwidth = bounds!!.width()
        val vheight = bounds.height()
        val fits = ((contentWidth < 0 || vwidth == contentWidth)
                && (contentHeight < 0 || vheight == contentHeight))
        if (contentWidth <= 0 || contentHeight <= 0 || ImageView.ScaleType.FIT_XY == scaleType) {
            //默认Matrix
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            if (ImageView.ScaleType.MATRIX == scaleType) {
                //调用方处理
                throw RuntimeException("ImageView.ScaleType.MATRIX == scaleType!!")
            } else if (fits) {
                // The bitmap fits exactly, no transform needed.
            } else if (ImageView.ScaleType.CENTER == scaleType) {
                // Center bitmap in view, no scaling.
                matrix.setTranslate(
                    Math.round((vwidth - contentWidth) * 0.5f).toFloat(),
                    Math.round((vheight - contentHeight) * 0.5f).toFloat()
                )
            } else if (ImageView.ScaleType.CENTER_CROP == scaleType) {
                val scale: Float
                var dx = 0f
                var dy = 0f
                if (contentWidth * vheight > vwidth * contentHeight) {
                    scale = vheight.toFloat() / contentHeight.toFloat()
                    dx = (vwidth - contentWidth * scale) * 0.5f
                } else {
                    scale = vwidth.toFloat() / contentWidth.toFloat()
                    dy = (vheight - contentHeight * scale) * 0.5f
                }
                matrix.setScale(scale, scale)
                matrix.postTranslate(Math.round(dx).toFloat(), Math.round(dy).toFloat())
            } else if (ImageView.ScaleType.CENTER_INSIDE == scaleType) {
                val scale: Float
                val dx: Float
                val dy: Float
                scale = if (contentWidth <= vwidth && contentHeight <= vheight) {
                    1.0f
                } else {
                    Math.min(
                        vwidth.toFloat() / contentWidth.toFloat(),
                        vheight.toFloat() / contentHeight.toFloat()
                    )
                }
                dx = Math.round((vwidth - contentWidth * scale) * 0.5f).toFloat()
                dy = Math.round((vheight - contentHeight * scale) * 0.5f).toFloat()
                matrix.setScale(scale, scale)
                matrix.postTranslate(dx, dy)
            } else {
                // Generate the required transform.
                val tempSrc = RectF()
                val tempDst = RectF()
                tempSrc[0f, 0f, contentWidth.toFloat()] = contentHeight.toFloat()
                tempDst[0f, 0f, vwidth.toFloat()] = vheight.toFloat()
                matrix.setRectToRect(tempSrc, tempDst, scaleTypeToScaleToFit(scaleType))
            }
        }
        return matrix
    }

    private fun scaleTypeToScaleToFit(st: ImageView.ScaleType?): Matrix.ScaleToFit? {
        // ScaleToFit enum to their corresponding Matrix.ScaleToFit values
        return sS2FArray[st!!.ordinal - 1]
    }

    private val sS2FArray = arrayOf(
        Matrix.ScaleToFit.FILL,
        Matrix.ScaleToFit.START,
        Matrix.ScaleToFit.CENTER,
        Matrix.ScaleToFit.END
    )

    fun isFrescoView(view: View): Boolean {
        return view.javaClass.name.startsWith("com.facebook.drawee")
    }
}