package com.zxj.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ZPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        /* 创建extension */
        val extension = project.extensions.create("zxj", ZBean::class.java)

        /* 第二阶段 -> 第三阶段 之间 */
        project.afterEvaluate {
            println("name = ${extension.name}")
        }

//        // 去干预android打包过程，所以取出 com.android.application 中的 BaseExtension
        val transform = ZTransform()
        val baseExtension = project.extensions.getByType(BaseExtension::class.java)
        baseExtension.registerTransform(transform, *arrayOf())
        /* transform */
//        project.extensions.tr
    }
}

