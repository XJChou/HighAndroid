package com.zxj.fragment.bean

import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import kotlinx.android.parcel.Parcelize

/**
 * 用于保存原有的内容
 * +
 * 新内容
 */
@Parcelize
class SharedElementItem(val originParcelable: Parcelable? = null) : Parcelable {

    companion object {
        private const val SHARED_ELEMENT_ORIGIN_TYPE = "SHARED_ELEMENT_ORIGIN"
        private const val ORIGIN_TYPE_TEXT_VIEW = 1

        const val TEXT_VIEW_SIZE = "TEXT_SIZE"
        const val TEXT_VIEW_COLOR = "TEXT_COLOR"
    }

    private val bundle = Bundle()

    /**
     * 存储内容View关键信息
     */
    fun save(view: View): SharedElementItem {
        when (view) {
            is TextView -> {
                bundle.putInt(SHARED_ELEMENT_ORIGIN_TYPE, ORIGIN_TYPE_TEXT_VIEW)
                bundle.putFloat(TEXT_VIEW_SIZE, view.textSize)
                bundle.putInt(TEXT_VIEW_COLOR, view.currentTextColor)
            }
        }
        return this
    }

    fun getInt(key: String): Int {
        return bundle.getInt(key)
    }

    fun getFloat(key: String): Float {
        return bundle.getFloat(key)
    }

    /**
     * 从关键信息恢复内容
     */
    fun restore(view: View): SharedElementItem {
        val type = bundle.getInt(SHARED_ELEMENT_ORIGIN_TYPE)
        when {
            type == ORIGIN_TYPE_TEXT_VIEW && view is TextView -> {
                val textSize = bundle.getFloat(TEXT_VIEW_SIZE)
                val textColor = bundle.getInt(TEXT_VIEW_COLOR)
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                view.setTextColor(textColor)
            }
        }
        return this
    }
}