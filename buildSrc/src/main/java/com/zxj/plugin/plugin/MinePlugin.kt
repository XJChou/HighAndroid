package com.zxj.plugin.plugin

import com.android.build.gradle.BaseExtension
import com.zxj.plugin.extension.Mine
import com.zxj.plugin.transform.MineTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class MinePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 注册扩展
        val mineExtension = project.extensions.create("mine", Mine::class.java)
        project.afterEvaluate {
            println("mine extension name value = ${mineExtension.name}")
        }

        // BaseExtension 注册 transform
        val baseExtension = project.extensions.getByType(BaseExtension::class.java)
        baseExtension.registerTransform(MineTransform())
    }

}