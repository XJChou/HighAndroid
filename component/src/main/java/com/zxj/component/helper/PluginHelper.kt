package com.zxj.component.helper

import android.content.Context
import dalvik.system.DexClassLoader


/**
 * 插件帮助类
 */
object PluginHelper {

    /**
     * 插件化
     */
    fun onPlugin(context: Context) {
        kotlin.runCatching {
            val file = context.assetToCacheFile("plugin.apk", "plugin.apk")

            // 1、classloader 加载 指定apk
            val classLoader = DexClassLoader(file.path, context.cacheDir.path, null, null)

            // 2、classLoader 加载指定类
            val pluginClass = classLoader.loadClass("com.zxj.highandroid.Plugin")

            // 3、生成指定类的对象
            val plugin = pluginClass.newInstance()

            // 4、调用指定方法
            val toastMethod = pluginClass.getDeclaredMethod("toast", Context::class.java)
            toastMethod.invoke(plugin, context)
        }
    }

}