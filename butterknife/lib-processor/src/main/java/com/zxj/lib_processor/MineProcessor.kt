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
        // EnclosingElemtn -> rootElements -> enclosedElements
        roundEnv.rootElements.forEach {
            // 获取当前类基本信息
            println("rootElements = ${it}")
            val packageString = it.enclosingElement.toString()
            val classString = it.simpleName.toString()

            val className = ClassName.get(packageString, "${classString}Binding")
            val methodBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(packageString, classString), "activity")

            var hasBinding = false

            it.enclosedElements.forEach { enclosedElement ->
                if (enclosedElement.kind == ElementKind.FIELD) {
                    val bindView = enclosedElement.getAnnotation(BindView::class.java)
                    if (bindView != null) {
                        hasBinding = true
                        // $N -> Name
                        // $L -> Literal(具体值)
                        methodBuilder.addStatement(
                            "activity.\$N = activity.findViewById(\$L)",
                            enclosedElement.simpleName, bindView.value
                        )
                    }
                }
            }

            val buildClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodBuilder.build())
                .build()

            if (hasBinding) {
                JavaFile.builder(packageString, buildClass)
                    .build().writeTo(javaFile)
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
