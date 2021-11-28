package com.zxj.common.bean

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import kotlinx.android.parcel.Parcelize

/**
 * 用于保存原有的内容
 * +
 * 新内容
 */
@Parcelize
class SharedElementItem private constructor(val originParcelable: Parcelable? = null) : Parcelable {

    companion object {
        private const val SHARED_ELEMENT_ORIGIN_TYPE = "SHARED_ELEMENT_ORIGIN"
        private const val ORIGIN_TYPE_TEXT_VIEW = 1

        const val TEXT_VIEW_SIZE = "TEXT_SIZE"
        const val TEXT_VIEW_COLOR = "TEXT_COLOR"

        fun create(view: View, originParcelable: Parcelable? = null) =
            SharedElementItem(originParcelable).save(view)
    }

    private val bundle = Bundle()

    /**
     * 存储内容View关键信息
     */
    private fun save(view: View): SharedElementItem {
        when (view) {
            is TextView -> {
                bundle.putInt(SHARED_ELEMENT_ORIGIN_TYPE, ORIGIN_TYPE_TEXT_VIEW)
                bundle.putFloat(TEXT_VIEW_SIZE, view.textSize)
                bundle.putInt(TEXT_VIEW_COLOR, view.currentTextColor)
            }
        }
        return this
    }

    fun getInt(key: String) = bundle.getInt(key)

    fun getFloat(key: String) = bundle.getFloat(key)
}