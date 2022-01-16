package com.zxj.lib

import android.app.Activity

class SimpleButterKnife {
    companion object {
        fun bind(activity: Activity) {
            val activityClass = activity::class.java
            val bindingName = activityClass.canonicalName + "Binding"
            val bindingClass = Class.forName(bindingName)

            val constructor = bindingClass.getDeclaredConstructor(activityClass)
            constructor.newInstance(activity)
        }
    }
}