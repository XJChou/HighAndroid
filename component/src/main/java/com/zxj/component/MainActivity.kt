package com.zxj.component

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zxj.component.databinding.ActivityMainBinding
import com.zxj.component.helper.HotfixHelper
import com.zxj.component.helper.PluginHelper
import com.zxj.component.helper.assetToCacheFile
import com.zxj.component.hotfix.Title
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexClassLoader

/**
 * 插件化 和 热更新 Demo
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val onclickListener = View.OnClickListener {
        when (it.id) {
            /* 插件化[OK] */
            R.id.btn_plugin -> {
                PluginHelper.onPlugin(this)
            }

            /* 实时替换包[OK] */
            R.id.btn_hotfix_full_load -> {
                fullHotfix()
            }

            /* 启动自动应用热更新 */
            R.id.btn_hotfix_path_load -> {
                HotfixHelper.prepareLoad(this)
            }
            R.id.btn_hotfix_path_unload -> {
                HotfixHelper.prepareUnload(this)
            }

            /* 验证 */
            R.id.btn_hotfix_toast -> {
                val title = Title()
                Toast.makeText(this, title.title, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 插件化内容
        binding.btnPlugin.setOnClickListener(onclickListener)

        // 全量临时替换式
        binding.btnHotfixFullLoad.setOnClickListener(onclickListener)

        // 部分替换式
        binding.btnHotfixPathLoad.setOnClickListener(onclickListener)
        binding.btnHotfixPathUnload.setOnClickListener(onclickListener)

        /* 验证 */
        binding.btnHotfixToast.setOnClickListener(onclickListener)
    }

    /**
     * apk全量替换式
     */
    private fun fullHotfix() {
        val file = assetToCacheFile("full-hotfix.apk", "full-hotfix.apk")

        try {
            val classLoader = DexClassLoader(file.path, cacheDir.path, null, null)
            val baseDexClass = BaseDexClassLoader::class.java

            val pathListField = baseDexClass.getDeclaredField("pathList")
            pathListField.isAccessible = true
            val pathList = pathListField[classLoader]

            val dexElementsField = pathList.javaClass.getDeclaredField("dexElements")
            dexElementsField.isAccessible = true
            val dexElements = dexElementsField[pathList]

            // originClassLoader.pathList.dexElements = newDexClassLoader.pathList.dexElements
            val originClassLoader = this.classLoader
            val originPathList = pathListField[originClassLoader]
            dexElementsField[originPathList] = dexElements
            Toast.makeText(this, "替换成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "替换异常", Toast.LENGTH_SHORT).show()
        }
    }
}

