package com.zxj.common

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.util.Log
import android.util.TypedValue
import android.view.View
import kotlin.reflect.KProperty

val Float.dp
    get() = run {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )
    }


val Int.dp
    get() = this.toFloat().dp

fun Resources.decodeResource(resourceId: Int, targetWidth: Int): Bitmap {
    val option = BitmapFactory.Options()
    option.inJustDecodeBounds = true
    BitmapFactory.decodeResource(this, resourceId, option)

    option.inJustDecodeBounds = false
    option.inDensity = option.outWidth
    option.inTargetDensity = targetWidth
    return BitmapFactory.decodeResource(this, resourceId, option)
}

fun Resources.decodeResource(resourceId: Int, targetWidth: Float): Bitmap {
    return decodeResource(resourceId, targetWidth.toInt())
}

class FloatInvalidate(var field: Float = 0f) {

    operator fun getValue(view: View, property: KProperty<*>) = field

    operator fun setValue(view: View, property: KProperty<*>, value: Float) {
        field = value
        view.invalidate()
    }

}

inline fun Camera.withSave(block: Camera.() -> Unit) {
    save()
    try {
        block()
    } finally {
        restore()
    }
}

inline fun e(msg: String) {
    Log.e("HighAndroid", msg)
}