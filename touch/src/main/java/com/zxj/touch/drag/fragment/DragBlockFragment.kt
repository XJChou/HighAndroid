package com.zxj.touch.drag.fragment

import androidx.fragment.app.Fragment
import com.zxj.touch.R

/**
 * 【作图题】按照课程中的内容，写一个可以纵向或者横向拖拽并且可以自动停靠的滑动控件，并上传效果动图。
 * [1] 能够自动回到合适位置[Finish]
 * [2] 能够交换位置并且有过度动画[Finish]
 */
class DragBlockFragment : Fragment(R.layout.fragment_drag_block) {
    companion object {
        fun newInstance() = DragBlockFragment()
    }
}