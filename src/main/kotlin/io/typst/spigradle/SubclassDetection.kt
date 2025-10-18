/*
 * Copyright (c) 2025 Spigradle contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.typst.spigradle

import io.typst.spigradle.detection.ClassDefinition
import io.typst.spigradle.detection.DetectionContext
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * Finds the main class that extends the given super-class.
 *
 * Groovy Example:
 *
 * ```groovy
 * import io.typst.spigradle.SubclassDetection
 *
 * task findSubclass(type: SubclassDetection) {
 *   superClassName = 'com.my.sample.SuperType'
 *   classDirectories.from sourceSets.main.output.classesDirs
 *   outputFile = file('result.txt')
 * }
 * ```
 *
 * ```kotiln
 * import io.typst.spigradle.SubclassDetection
 *
 * tasks {
 *   val findSubclass(type: SubclassDetection) {
 *     superClassName.set("com.my.sample.SuperType")
 *     classDirectories.from(sourceSets["main"].output.classesDirs)
 *     outputFile.set(file("result.txt"))
 *   }
 * }
 * ```
 *
 * @since 1.3.0
 */
open class SubclassDetection : DefaultTask() {
    init {
        group = "spigradle"
        description = "Detect the jvm subclass."
    }

    /**
     * The name of super-class used to detect a sub-class.
     */
    @get:Input
    val superClassName: Property<String> = project.objects.property()

    /**
     * The class directories used to target of the sub-class detection.
     */
    @get:SkipWhenEmpty
    @get:InputFiles
    val classDirectories: ConfigurableFileCollection = project.objects.fileCollection()

    /**
     * The path of plain text includes the detection result.
     */
    @get:OutputFile
    val outputFile = project.objects.property<File>()

    @TaskAction
    fun inspect(inputChanges: InputChanges) {
        val contextR = AtomicReference(DetectionContext())
        val options = ClassReader.SKIP_CODE and ClassReader.SKIP_DEBUG and ClassReader.SKIP_FRAMES

        val parentName = superClassName.get()
        val fileChanges = inputChanges.getFileChanges(classDirectories)
        for (change in fileChanges) {
            val file = change.file
            if (file.extension == "class" && file.isFile) {
                file.inputStream().buffered().use {
                    ClassReader(it).accept(SubclassDetector(contextR), options)
                }
            }
            if (contextR.get().findMainClass(parentName) != null) {
                break
            }
        }
        val detectedClass = contextR.get().findMainClass(parentName)
        if (detectedClass != null) {
            outputFile.get().apply {
                parentFile.mkdirs()
            }.writeText(detectedClass.name.replace('/', '.'))
        }
    }

    companion object {
        fun register(project: Project, taskName: String, type: String): TaskProvider<SubclassDetection> {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class)
            return project.tasks.register(taskName, SubclassDetection::class) {
                val pathFile = project.getPluginMainPathFile(type)
                val compileJava = project.tasks.named<JavaCompile>("compileJava")
                dependsOn(compileJava)
                /*
                NOTE:
                If put a FileCollection into the `from` makes this task ordered after `classes`.
                Therefore put List<File> instead.
                 */
                classDirectories.from(sourceSets["main"].output.classesDirs.files)
                outputFile.convention(pathFile) // defaults to pathFile
            }
        }
    }
}

internal class SubclassDetector(
    private val contextR: AtomicReference<DetectionContext>,
) : ClassVisitor(Opcodes.ASM9) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?,
    ) {
        val classDef = ClassDefinition.fromASM(access, name, superName)
        contextR.updateAndGet { ctx ->
            ctx.addClassDef(classDef)
        }
    }
}
