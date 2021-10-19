package com.zxj.touch.drag

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.zxj.common.PointEvaluator
import com.zxj.touch.R

private const val ROW_COUNT = 3
private const val COLUMN_COUNT = 2

/**
 * 【作图题】按照课程中的内容，写一个可以纵向或者横向拖拽并且可以自动停靠的滑动控件，并上传效果动图。
 * [1] 能够自动回到合适位置[Finish]
 * [2] 能够交换位置并且有过度动画[Finish]
 */
class DragBlockView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    private val viewDragHelper = ViewDragHelper.create(this, DragHelperCallback())

    // 实际位置
    private val childLayoutPositions = arrayListOf<Int>()
    private val rects = arrayListOf<Rect>()

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val originHeightSize = MeasureSpec.getSize(heightMeasureSpec)

        val childWidthSize = originWidthSize / COLUMN_COUNT
        val childHeightSize = originHeightSize / ROW_COUNT
        measureChildren(
            MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY)
        )

        val childCount = childCount
        for (i in 0 until childCount) {
            val offsetX = (i % 2) * childWidthSize
            val offsetY = (i / 2) * childHeightSize
            if (rects.size == i) {
                rects.add(Rect())
            }
            with(rects[i]) {
                left = offsetX
                top = offsetY
                right = offsetX + childWidthSize
                bottom = offsetY + childHeightSize
            }

            if (childLayoutPositions.size == i) {
                childLayoutPositions.add(i)
            } else {
                childLayoutPositions[i] = i
            }
        }

        setMeasuredDimension(originWidthSize, originHeightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val rect = rects[childLayoutPositions[i]]
            getChildAt(i).layout(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return viewDragHelper.shouldInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!viewDragHelper.continueSettling(false)) {
            viewDragHelper.processTouchEvent(event)
        }
        return true
    }

    private fun findChildView(x: Float, y: Float): Int {
        for (i in rects.indices) {
            val touchX = x.toInt() + scrollX
            val touchY = y.toInt() + scrollY
            val realIndex = childLayoutPositions[i]
            if (touchX in rects[realIndex].left..rects[realIndex].right && touchY in rects[realIndex].top..rects[realIndex].bottom) {
                return i
            }
        }
        return -1
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun computeScroll() {
        super.computeScroll()
        if (viewDragHelper.continueSettling(false)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    inner class DragHelperCallback : ViewDragHelper.Callback() {

        private var holdChildIndex = 0

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return top
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            super.onViewCaptured(capturedChild, activePointerId)
            // 修改view层级
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.elevation = if (child != capturedChild) {
                    0f
                } else {
                    holdChildIndex = i
                    1f
                }
            }
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            val realLeft = left + changedView.width / 2
            val realTop = top + changedView.height / 2
            val newIndex = findChildView(realLeft.toFloat(), realTop.toFloat())
            if (newIndex != -1 && holdChildIndex != newIndex) {
                // 所在位置交换
                val layoutPosition = childLayoutPositions[holdChildIndex]
                childLayoutPositions[holdChildIndex] = childLayoutPositions[newIndex]
                childLayoutPositions[newIndex] = layoutPosition

                /* 更新子view位置 */
                var originRect = rects[childLayoutPositions[newIndex]]
                val child = getChildAt(newIndex)
                moveChild(child, originRect.left, originRect.top)
            }
        }

        private fun moveChild(child: View, left: Int, top: Int) {
            var animator = child.getTag(R.id.actions) as? ValueAnimator
            if (animator == null) {
                animator = ValueAnimator.ofObject(PointEvaluator(), Point(), Point())
                animator.addUpdateListener {
                    child.offsetPoint = it.animatedValue as Point
                }
                child.setTag(R.id.actions, animator)
            }
            animator?.also {
                it.cancel()
                it.setObjectValues(child.offsetPoint, Point(left, top))
                it.start()
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)
            val rect = rects[childLayoutPositions[holdChildIndex]]

            // 扔物线：跟整体速度有关，且自动是上次view   -   滚动到自定位置
            viewDragHelper.settleCapturedViewAt(rect.left, rect.top)

            // 我：找的是路程除以总路程当速度 且 要传入上一个view   -   滚动到自定位置
//            viewDragHelper.smoothSlideViewTo(releasedChild, rect.left, rect.top)
            ViewCompat.postInvalidateOnAnimation(this@DragBlockView)
        }
    }
}

private var View.offsetPoint: Point
    set(value) {
        offsetLeftAndRight(value.x - left)
        offsetTopAndBottom(value.y - top)
        invalidate()
    }
    get() = Point(this.left, this.top)

