package com.zxj.lib_processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.zxj.lib_annotation.BindView
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

class MineProcessor : AbstractProcessor() {

    private lateinit var javaFile: Filer

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        javaFile = processingEnv.filer
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        // 每个Activity的遍历
        roundEnv.rootElements.forEach {
            // 当前 RootElement 的包名和类名
            val packageName = it.enclosingElement.toString()    // 用于包裹 RootElement，这里是包名
            val className = it.simpleName.toString()            // Element 类名字

            // 生成 一个带 RootElement(Activity) 参数的构造方法
            val constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(packageName, className), "activity")

            var isNeedBuild = false
            // 内容为 @BindView 注解 生成 activity.* = activity.findViewById(*)
            it.enclosedElements
                .filter { it.kind == ElementKind.FIELD }
                .forEach {
                    val bindView = it.getAnnotation(BindView::class.java)
                    if (bindView != null) {
                        isNeedBuild = true
                        val viewId = bindView.value
                        constructorBuilder.addStatement(
                            "activity.\$N = activity.findViewById(\$L)",
                            it.simpleName, viewId
                        )
                    }
                }

            // package + public [className]Binding + 构造方法
            val bindingType = TypeSpec
                .classBuilder("${className}Binding")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructorBuilder.build())
                .build()

            // 需要构建文件
            if (isNeedBuild) {
                JavaFile
                    .builder(packageName, bindingType)
                    .build()
                    .writeTo(javaFile)
            }
        }

        return false
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        val canonicalName = BindView::class.java.canonicalName
        return Collections.singleton(canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.RELEASE_8
}
