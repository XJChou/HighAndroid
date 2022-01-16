package com.zxj.lib_annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class BindView(
    val value: Int
)