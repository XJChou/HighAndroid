package com.zxj.customlayout.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import kotlin.math.max

val PROVINCE = arrayOf(
    /* 市 */
    "北京市",
    "天津市",
    "上海市",
    "重庆市",
    /* 省份 */
    "河北省",
    "山西省",
    "辽宁省",
    "吉林省",
    "黑龙江省",
    "江苏省",
    "浙江省",
    "安徽省",
    "福建省",
    "江西省",
    "山东省",
    "河南省",
    "湖北省",
    "湖南省",
    "广东省",
    "海南省",
    "四川省",
    "贵州省",
    "云南省",
    "陕西省",
    "甘肃省",
    "青海省",
    "台湾省",
    /* 自治区 */
    "内蒙古自治区",
    "广西壮族自治区",
    "西藏自治区",
    "宁夏回族自治区",
    "新疆维吾尔自治区",
    /* 行政区 */
    "香港特别行政区",
    "澳门特别行政区"
)

/**
 * 简单版TabLayout
 */
class TabLayout(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    init {
        PROVINCE.forEach {
            val tabView = TabView(context)
            tabView.text = it
            addView(tabView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
    }

    /* 子控件的左上定位 */
    private var lefts = mutableListOf<Int>()
    private var tops = mutableListOf<Int>()

    /**
     * 测量子view和自身的宽高
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                MeasureSpec.getSize(widthMeasureSpec)
            }
            else -> {
                Int.MAX_VALUE
            }
        }

        lefts.clear()
        tops.clear()

        // 起点 start 和 top
        var start = 0
        var top = 0

        /* 当前最大宽是多少 */
        var maxWidth = 0
        /* 每一行最大高是多少 */
        var maxLineHeight = 0

        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            /* 1、测量当前子view的宽度和布局 */
            child.measure(
                getMeasureSpec(child.layoutParams.width, widthMeasureSpec),
                getMeasureSpec(child.layoutParams.height, heightMeasureSpec)
            )
//            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)

            /* 2、计算 view 的 left 和 top 布局 */
            val childMeasuredWidth = child.measuredWidth
            val childMeasuredHeight = child.measuredHeight

            /* 需要换行才能撑满了 */
            if (childMeasuredWidth + start > parentWidth) {
                top += maxLineHeight

                start = 0
                maxLineHeight = 0
            }

            /* 记录当前view的布局 */
            lefts.add(start)
            tops.add(top)

            /* 更新值 */
            start += childMeasuredWidth
            maxLineHeight = max(maxLineHeight, childMeasuredHeight)

            maxWidth = max(maxWidth, start)

        }

        /* 3、得出算出父空间整体的宽高 */
        setMeasuredDimension(maxWidth, top + maxLineHeight)
    }


    private fun getMeasureSpec(devRequest: Int, parentMeasureSpec: Int): Int {
        /* 当前父布局要求 */
        val parentMode = MeasureSpec.getMode(parentMeasureSpec)
        val parentSize = MeasureSpec.getSize(parentMeasureSpec)
        return when (devRequest) {
            LayoutParams.MATCH_PARENT, LayoutParams.FILL_PARENT -> {
                when (parentMode) {
                    /* 父布局是 EXACTLY + 开发者填的是 MATCH_PARENT */
                    MeasureSpec.EXACTLY -> {
                        MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.EXACTLY)
                    }
                    /* 父布局是 AT_MOST + 开发者填的是 MATCH_PARENT */
                    MeasureSpec.AT_MOST -> {
                        // 这里 MeasureSpec.EXACTLY || MeasureSpec.AT_MOST 都行，不同策略
                        // AT_MOST 给予 子类充分自由
                        MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST)
                    }
                    else -> {
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    }
                }
            }
            LayoutParams.WRAP_CONTENT -> {
                when (parentMode) {
                    /* 父布局是 EXACTLY + 开发者填的是 WRAP_CONTENT */
                    MeasureSpec.EXACTLY -> {
                        MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST)
                    }
                    /* 父布局是 AT_MOST + 开发者填的是 WRAP_CONTENT */
                    MeasureSpec.AT_MOST -> {
                        MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST)
                    }
                    else -> {
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    }
                }
            }
            /* 开发者说要这么大，则按开发者要求 */
            else -> {
                MeasureSpec.makeMeasureSpec(devRequest, MeasureSpec.EXACTLY)
            }
        }

    }

    /**
     * 根据测量的位置开始布局
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val start = lefts[i]
            val top = tops[i]
            child.layout(start, top, start + child.measuredWidth, top + child.measuredHeight)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
//        return super.generateLayoutParams(attrs)
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

//
//    /**
//     * 生成子view的LayoutParams
//     */
//    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
////        return super.generateLayoutParams(attrs)
//        return MarginLayoutParams(context, attrs)
//    }

    /**
     * 标识当前ViewGroup不可滚动
     */
    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }
}