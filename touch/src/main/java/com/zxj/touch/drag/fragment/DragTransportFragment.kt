package com.zxj.touch.drag.fragment

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.zxj.touch.R

/**
 * 【作图题】按照课程中的内容，写一个可以长按后拖起半透明图像的控件，并且当手指把这个图像放在界面中某一个位置的时候松手，
 * 界面中会显示出相应的效果表示「落在这里了」（可以不像课程中一样动态增加 TextView，选用别的方式也行，例如弹一个 Toast）。并上传效果动图。
 */
class DragTransportFragment : Fragment(R.layout.fragment_drag_transport), View.OnDragListener {

    companion object {
        fun newInstance() = DragTransportFragment()
    }

    private lateinit var ivRole: ImageView
    private lateinit var tvLeft: TextView
    private lateinit var tvRight: TextView
    private lateinit var tvTop: TextView
    private lateinit var tvCenter: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivRole = view.findViewById(R.id.iv_role)
        tvLeft = view.findViewById(R.id.tv_left)
        tvRight = view.findViewById(R.id.tv_right)
        tvTop = view.findViewById(R.id.tv_top)
        tvCenter = view.findViewById(R.id.tv_center)

        ivRole.setOnLongClickListener {
            ViewCompat.startDragAndDrop(
                it,
                null,
                View.DragShadowBuilder(it),
                "扔老师",
                View.DRAG_FLAG_OPAQUE
            )
            true
        }

        tvLeft.setOnDragListener(this)
        tvTop.setOnDragListener(this)
        tvRight.setOnDragListener(this)
        tvCenter.setOnDragListener(this)
        tvCenter.movementMethod = ScrollingMovementMethod()

    }

    override fun onDrag(v: View, event: DragEvent): Boolean {
        Log.e(javaClass.simpleName, "event action = ${event.action}")
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                if (v == tvLeft) {
                    tvCenter.text = tvCenter.text.toString() + "${event.localState}出左边界了\n"
                } else if (v == tvRight) {
                    tvCenter.text = tvCenter.text.toString() + "${event.localState}出右边界了\n"
                } else if (v == tvTop) {
                    tvCenter.text = tvCenter.text.toString() + "${event.localState}踢出世界波了!!!\n"
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (v == tvCenter) {
                    Toast.makeText(
                        requireActivity(),
                        "${event.localState}这里是解说席",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        return true
    }

}