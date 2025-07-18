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

import kr.entree.spigradle.annotations.PluginType
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.get
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
        val superClassesR = AtomicReference(setOf(superClassName.get()))
        val detectedClassR = AtomicReference<String?>(null)
        val options = ClassReader.SKIP_CODE and ClassReader.SKIP_DEBUG and ClassReader.SKIP_FRAMES
        inputChanges.getFileChanges(classDirectories).asSequence().takeWhile {
            detectedClassR.get() == null
        }.map {
            it.file
        }.filter {
            it.extension == "class" && it.isFile
        }.forEach { classFile ->
            classFile.inputStream().buffered().use {
                ClassReader(it).accept(SubclassDetector(superClassesR, detectedClassR), options)
            }
        }
        val detectedClass = detectedClassR.get()
        // Add `-i` option to see this log!
        logger.info("detected: ${detectedClass?.plus(".java")}")
        if (detectedClass != null) {
            outputFile.get().apply {
                parentFile.mkdirs()
            }.writeText(detectedClass.replace('/', '.'))
        }
    }

    companion object {
        fun register(project: Project, taskName: String, type: PluginType): TaskProvider<SubclassDetection> {
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
                onlyIf {
                    !pathFile.isFile
                }
            }
        }
    }
}

// TODO: Optimize scheduled in 2.3.0!
internal fun findSubclass(
    supers: Set<String>,
    access: Int, name: String, superName: String?
): Pair<String?, Set<String>> {
    return if (superName in supers) {
        val sub = if (access.isPublic && !access.isAbstract) name else null
        val newSupers = if (access.isAbstract) supers + name else supers
        (sub to newSupers)
    } else (null to supers)
}

internal val Int.isPublic get() = (this and Opcodes.ACC_PUBLIC) != 0
internal val Int.isAbstract get() = (this and Opcodes.ACC_ABSTRACT) != 0

class SubclassDetector(
    private val supersR: AtomicReference<Set<String>>,
    private val detectedR: AtomicReference<String?>
) : ClassVisitor(Opcodes.ASM8) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        val (sub, supers) = findSubclass(supersR.get(), access, name, superName)
        supersR.set(supers)
        detectedR.updateAndGet { it ?: sub }
    }
}
