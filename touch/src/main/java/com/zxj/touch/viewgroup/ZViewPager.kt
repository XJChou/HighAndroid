package com.zxj.touch.viewgroup

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.children
import com.zxj.common.dp
import kotlin.math.abs

/**
 * 允许超出距离
 */
private val OVER_DISTANCE = 50.dp

/**
 * 简易ViewPager
 * (1) 支持多页【但不支持复用】
 * (2) 支持手势滚动[支持抢夺子类事件]
 * (3) 支持惯性滑动
 */
class ZViewPager(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    /**
     * 系统默认view的数值
     */
    private val viewConfiguration = ViewConfiguration.get(context)
    val minVelocity = viewConfiguration.scaledMinimumFlingVelocity
    val maxVelocity = viewConfiguration.scaledMaximumFlingVelocity
    val pageSlop = viewConfiguration.scaledPagingTouchSlop

    private val touch: IViewPageTouch = ZTouch2(this)

    var minScrollX = 0
        private set
    var maxScrollX = 0
        private set

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var offsetX = 0
        children.forEach { child ->
            child.layout(l + offsetX, t, r + offsetX, b)
            offsetX += r - l
        }
        minScrollX = 0
        maxScrollX = (childCount - 1) * width
    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return touch.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return touch.onTouchEvent(event)
    }

    override fun computeScroll() {
        super.computeScroll()
        touch.computeScroll()
    }
}

interface IViewPageTouch {
    fun onInterceptTouchEvent(event: MotionEvent): Boolean

    fun onTouchEvent(event: MotionEvent): Boolean

    fun computeScroll() {}

    fun getTargetScrollX(scrollX: Int, xVelocity: Float, view: ZViewPager): Int {
        val targetIndex = if (abs(xVelocity) > view.minVelocity) {
            if (xVelocity > 0) {
                scrollX / view.width
            } else {
                scrollX / view.width + 1
            }
        } else {
            (scrollX + view.width / 2) / view.width
        }

        return (targetIndex * view.width)
            .coerceAtLeast(view.minScrollX)
            .coerceAtMost(view.maxScrollX)
    }
}


/**
 * 凯哥思路比我好的部分：
 * [1] 在点击的时候记录的 down 的坐标，使用每次的 move 与 down 坐标相比从而确定拦截，使得触发更容易【优化用户体验】
 * [2] postInvalidateOnAnimation 每一帧才刷新，减少无用刷新量
 * [3] 使用系统推荐的参数 ViewConfiguration，会有更好的体验
 *
 * 我优化部分
 * [1] 但确定拦截后，还使用move - down事件，这里觉得有点不当，当值比较大的时候可能会跳动严重
 * [2] 优化计算停靠位置
 * [3] 增加down事件停止动画
 */
class ZTouch2(private val view: ZViewPager) : IViewPageTouch {

    private val overScroller = OverScroller(view.context)

    private val velocityTracker = VelocityTracker.obtain()

    private var downX = 0f
    private var lastX = 0f

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear()
            overScroller.abortAnimation()
        }

        velocityTracker.addMovement(event)
        var isIntercept = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - downX) >= view.pageSlop) {
                    isIntercept = true
                    view.requestDisallowInterceptTouchEvent(true)
                    lastX = event.x
                }
            }
        }
        return isIntercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear()
            overScroller.abortAnimation()
        }

        velocityTracker.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = (view.scrollX - (event.x - lastX))
                    .coerceAtLeast(view.minScrollX.toFloat() - OVER_DISTANCE)
                    .coerceAtMost(view.maxScrollX.toFloat() + OVER_DISTANCE)
                lastX = event.x

                view.scrollTo(deltaX.toInt(), 0)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker.computeCurrentVelocity(1000, view.maxVelocity.toFloat())
                val scrollX = view.scrollX
                val xVelocity = velocityTracker.xVelocity
                val targetScrollX = getTargetScrollX(scrollX, xVelocity, view)
                overScroller.startScroll(scrollX, 0, targetScrollX - scrollX, 0, 500)
                view.postInvalidateOnAnimation()
            }
        }

        return true
    }

    override fun computeScroll() {
        if (overScroller.computeScrollOffset()) {
            view.scrollTo(overScroller.currX, 0)
            view.postInvalidateOnAnimation()
        }
    }
}


class ZTouch(private val view: ZViewPager) : IViewPageTouch {

    private var lastX = 0f
    private val velocityTracker = VelocityTracker.obtain()
    private val animator by lazy {
        ObjectAnimator.ofInt(view, "scrollX", 0, 0).also {
            it.interpolator = DecelerateInterpolator()
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear()
            animator.cancel()
        }

        velocityTracker.addMovement(event)
        var isIntercept = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(event.x - lastX)
                lastX = event.x
                if (deltaX > view.pageSlop) {
                    isIntercept = true
                    view.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
        }
        return isIntercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear()
            animator.cancel()
        }

        velocityTracker.addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val delta = event.x - lastX
                lastX = event.x

                var nextScrollX = (view.scrollX - delta.toInt())
                    .coerceAtLeast((view.minScrollX - OVER_DISTANCE).toInt())
                    .coerceAtMost((view.maxScrollX + OVER_DISTANCE).toInt())
                view.scrollTo(nextScrollX, 0)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker.computeCurrentVelocity(50)
                val scrollX = view.scrollX
                val xVelocity = velocityTracker.xVelocity
                val targetScrollX = getTargetScrollX(scrollX, xVelocity, view)
                animator.setIntValues(scrollX, targetScrollX)
                animator.start()
            }
        }
        return true
    }
}