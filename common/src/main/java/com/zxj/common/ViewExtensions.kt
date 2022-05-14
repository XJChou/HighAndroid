package com.zxj.common

import android.view.View
import androidx.core.view.*

class InitialPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

class InitialMargin(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft,
    view.paddingTop,
    view.paddingRight,
    view.paddingBottom
)

fun recordInitialMarginForView(view: View) = InitialMargin(
    view.marginLeft,
    view.marginTop,
    view.marginRight,
    view.marginBottom
)

fun View.doOnApplyWindowInsets(block: (WindowInsetsCompat, InitialPadding, InitialMargin) -> Unit) {
    val initialPadding = recordInitialPaddingForView(this)
    val initialMargin = recordInitialMarginForView(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        block(insets, initialPadding, initialMargin)
        // 兼容 android11之前的版本，返回原先内容
        insets
    }
    requestApplyWhenAttached()
}

fun View.requestApplyWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}