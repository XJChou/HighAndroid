package com.zxj.plugin.transform

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.util.GFileUtils
import org.objectweb.asm.*
import org.objectweb.asm.Type.LONG_TYPE
import org.objectweb.asm.Type.getType
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class MineTransform : Transform() {

    override fun getName(): String = MineTransform::class.java.simpleName

    // 输入的文件类型
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_JARS

    // 输入的范围
    override fun getScopes(): MutableSet<QualifiedContent.ScopeType> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental() = false

    // 从 .gradle 文件夹 拷贝到 transform
    override fun transform(transformInvocation: TransformInvocation) {
        val outputProvider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach {
                val dest = outputProvider.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.DIRECTORY
                )
                transform(it.file, dest)
            }

            // 从 Jar -> 目标transform目录
            input.jarInputs.forEach {
                val dest = outputProvider.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.JAR
                )
                transform(it.file, dest)
            }
        }
    }

    /**
     * 仅转换transform
     */
    private fun onlyTransform(transformInvocation: TransformInvocation) {
        val provider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach {
            it.directoryInputs.forEach {
                val dest = provider
                    .getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                GFileUtils.copyDirectory(it.file, dest)
                println("source: ${it.file}")
                println("dest: ${dest}\n")
            }

            it.jarInputs.forEach {
                val dest = provider
                    .getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                GFileUtils.copyFile(it.file, dest)
                println("source: ${it.file}")
                println("dest: ${dest}\n")
            }
        }
    }

    /**
     * 处理 direction input
     * 一个一个移植
     */
    private fun transform(source: File, dest: File) {
        when {
            source.isDirectory -> {
                // 拷贝目录
                source.listFiles()?.forEach {
                    val newDest = File(dest, it.name)
                    transform(it, newDest)
                }
            }
            source.isFile -> {
                asmTransform(source, dest)
            }
        }
    }

    private fun asmTransform(source: File, dest: File) {
        if (!source.name.endsWith(".class")) {
            source.copyTo(dest, true)
            return
        }

        kotlin
            .runCatching {
                println(" ----------------------- asm start[${source.name}] ----------------------- ")
                // 从文件读入内存
                val fileInputStream = FileInputStream(source)
                val classReader = ClassReader(fileInputStream)
                val classWriter = ClassWriter(classReader, 0)
                // 阅读的模式、 - 关联打包的性能
                classReader.accept(MineVisitor(classWriter), ClassReader.SKIP_FRAMES)

                // 从内存写出到目标文件
                GFileUtils.touch(dest)           // 创建了一个新文件
                val fileOutputStream = FileOutputStream(dest)
                fileOutputStream.write(classWriter.toByteArray())
                fileOutputStream.close()
            }
            .onFailure {
                println("asm error: ${it}")
                source.copyTo(dest, true)
            }
    }

}

class MineVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, classVisitor) {

    // 类的名字
    private var className = ""

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        println("visit name = ${className}")
        // 增加一个 私有成员变量 为 startTime
        this.visitField(Opcodes.ACC_PRIVATE, "startTime", "J", null, null)
    }

    // 遍历
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        // 只能成员变量访问
        if (access.and(Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return MethodTimeVisitor(api, methodVisitor, access, name, descriptor)
    }
}

class MethodTimeVisitor(
    api: Int,
    originMethod: MethodVisitor,
    access: Int,
    name: String?,
    descriptor: String?
) : AdviceAdapter(api, originMethod, access, name, descriptor) {

    var timeIndex = 0

    override fun onMethodEnter() {
        super.onMethodEnter()
        // 执行 System.nanoTime()
        invokeStatic(getType("Ljava/lang/System;"), Method("nanoTime", "()J"))
        // 存入 本地本量表中
        timeIndex = newLocal(Type.LONG_TYPE)
        println("onMethodEnter timeIndex = ${timeIndex}")
        storeLocal(timeIndex)
    }

    override fun onMethodExit(code: Int) {
        super.onMethodExit(code)
        visitLdcInsn("zxj")

        buildMessage()
        invokeStatic(
            Type.getType("Landroid/util/Log;"),
            Method("e", "(Ljava/lang/String;Ljava/lang/String;)I")
        )
        pop()
    }

    private fun buildMessage() {
        // 完成后，此时操作栈["zxj", StringBuilder]
        newInstance(getType(StringBuilder::class.java))
        dup()
        invokeConstructor(getType(StringBuilder::class.java), Method("<init>", "()V"))
        visitLdcInsn("method use time = ")
        invokeVirtual(
            getType(StringBuilder::class.java),
            Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
        )


        invokeStatic(Type.getType(System::class.java), Method("nanoTime", "()J"))
        loadLocal(timeIndex)
        math(SUB, LONG_TYPE)
        invokeVirtual(
            getType(StringBuilder::class.java),
            Method("append", "(J)Ljava/lang/StringBuilder;")
        )


        visitLdcInsn("ns")
        invokeVirtual(
            getType(StringBuilder::class.java),
            Method("append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
        )


        invokeVirtual(
            getType(StringBuilder::class.java),
            Method("toString", "()Ljava/lang/String;")
        )
    }

}