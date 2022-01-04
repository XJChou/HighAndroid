package com.zxj.plugin.transform

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.util.GFileUtils
import org.objectweb.asm.*
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

    private fun transformOnly(transformInvocation: TransformInvocation) {
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
//                    println("base = ${dest}\nsource = ${it}\nfinal = ${newDest}\n")
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
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                classReader.accept(MineVisitor(classWriter), ClassReader.EXPAND_FRAMES)

                // 从内存写出到目标文件
                GFileUtils.touch(dest)           // 创建了一个新文件
                val fileOutputStream = FileOutputStream(dest)
                fileOutputStream.write(classWriter.toByteArray())
                fileOutputStream.close()
                classWriter::class.java.genericSuperclass
                println(" ----------------------- asm end ----------------------- ")
            }
            .onFailure {
                println("asm error: ${it}")
                source.copyTo(dest, true)
            }
    }

}

class MineVisitor(classWriter: ClassWriter) : ClassVisitor(Opcodes.ASM7, classWriter) {

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
        val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val count = (descriptor?.count { it == ';' } ?: 0) + 1
        println("method = ${name}, param = ${count}, descriptor = ${descriptor}")
        return MethodTimeVisitor(className, count, api, visitor)
    }
}

class MethodTimeVisitor(
    val className: String,
    val localCount: Int,
    api: Int,
    originMethod: MethodVisitor
) : MethodVisitor(api, originMethod) {

    // 方法前执行
    override fun visitCode() {
        super.visitCode()
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        // 存入本地栈中 [0为 this]
        mv.visitVarInsn(Opcodes.LSTORE, localCount);
    }

    /**
     * 发现内部操作指令
     */
    override fun visitInsn(opcode: Int) {
        // 如果是 RETURN 指令，代表防止结束
        if (opcode == Opcodes.RETURN) {
            mv.visitLdcInsn("zxj")
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/StringBuilder",
                "<init>",
                "()V",
                false
            )
            mv.visitLdcInsn("method use time = ")
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false
            )
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/System",
                "nanoTime",
                "()J",
                false
            )
            mv.visitVarInsn(Opcodes.LLOAD, localCount)
            mv.visitInsn(Opcodes.LSUB)
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "append",
                "(J)Ljava/lang/StringBuilder;",
                false
            )
            mv.visitLdcInsn("ns")
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false
            )
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/StringBuilder",
                "toString",
                "()Ljava/lang/String;",
                false
            )
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "android/util/Log",
                "e",
                "(Ljava/lang/String;Ljava/lang/String;)I",
                false
            )
            mv.visitInsn(Opcodes.POP)

//            mv.visitLdcInsn("zxj")
//            mv.visitLdcInsn("method[${name}] use time = ${delta}ns")
//            mv.visitMethodInsn(
//                Opcodes.INVOKESTATIC,
//                "android/util/Log",
//                "e",
//                "(Ljava/lang/String;Ljava/lang/String;)I",
//                false
//            )
//            mv.visitInsn(Opcodes.POP);
        }
        super.visitInsn(opcode)
    }
}