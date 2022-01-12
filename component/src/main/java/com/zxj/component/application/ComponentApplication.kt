package com.zxj.component.application

import android.app.Application
import android.content.Context
import com.zxj.component.helper.HotfixHelper

class ComponentApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        HotfixHelper.applyHotfix(this)
    }
}