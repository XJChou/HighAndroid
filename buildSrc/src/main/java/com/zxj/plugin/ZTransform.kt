package com.zxj.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.gradle.internal.pipeline.TransformManager

class ZTransform : Transform() {
    override fun getName() = "ZTransform"

    /* 需要操作那些文件 */
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    /* 范围是哪里 TransformManager.SCOPE_FULL_PROJECT */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT
//        ImmutableSet.of(
//        QualifiedContent.Scope.PROJECT,
//        QualifiedContent.Scope.SUB_PROJECTS,
//    )

    override fun isIncremental(): Boolean = false
}