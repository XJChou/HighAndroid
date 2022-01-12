package com.zxj.component.helper

import android.content.Context
import android.widget.Toast
import dalvik.system.BaseDexClassLoader
import dalvik.system.PathClassLoader


/**
 * 热更新帮助类
 */
object HotfixHelper {

    private const val CONFIG = "CONFIG"
    private const val KEY_APPLY_HOTFIX = "KEY_APPLY_HOTFIX"

    fun prepareLoad(context: Context) {
        val sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean(KEY_APPLY_HOTFIX, true)
            .apply()
    }

    fun prepareUnload(context: Context) {
        val sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean(KEY_APPLY_HOTFIX, false)
            .apply()
    }

    /**
     * 加载补丁包
     */
    fun applyHotfix(context: Context) {
        val sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE)
        val applyHotfix = sp.getBoolean(KEY_APPLY_HOTFIX, false)
        if (!applyHotfix) return

        val file = context.assetToCacheFile("path-hotfix.dex", "path-hotfix.dex")
        val hotfixClassLoader = PathClassLoader(file.path, null)
        val classLoader = HotfixHelper::class.java.classLoader

        // 构建dexPath
        val baseDexClassLoaderClass = BaseDexClassLoader::class.java
        val pathListField = baseDexClassLoaderClass.getDeclaredField("pathList").also {
            it.isAccessible = true
        }

        val oldPathList = pathListField.get(classLoader)
        val pathListClass = oldPathList::class.java
        val dexElementsField = pathListClass.getDeclaredField("dexElements").also {
            it.isAccessible = true
        }
        val dexElements = dexElementsField.get(oldPathList) as Array<*>

        val hotfixPathList = pathListField.get(hotfixClassLoader)
        val hotfixDexElements = dexElementsField.get(hotfixPathList) as Array<*>

        // hotfixDexElement + dexElements 组合
        val newDexElements = java.lang.reflect.Array.newInstance(
            dexElements::class.java.componentType!!,
            hotfixDexElements.size + dexElements.size
        )
        System.arraycopy(hotfixDexElements, 0, newDexElements, 0, hotfixDexElements.size)
        System.arraycopy(dexElements, 0, newDexElements, hotfixDexElements.size, dexElements.size)
        dexElementsField.set(oldPathList, newDexElements)

        Toast.makeText(context, "加载完成", Toast.LENGTH_SHORT).show()
    }
}
