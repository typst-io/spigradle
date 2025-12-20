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
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.log

internal fun Project.getSubclassDetectionOutputFile(platformName: String, propertyName: String): Provider<RegularFile> =
    layout.buildDirectory.file("spigradle/${platformName}_${propertyName}_fallback")

internal fun Project.getSubclassDetectionFallbackProperty(
    platformName: String,
    property: PluginDescriptionProperty,
): Provider<PluginDescriptionProperty> =
    getSubclassDetectionOutputFile(platformName, property.name).map {
        property.copy(valueFallbackFile = it)
    }

/**
 * Finds the main class that extends the given super-class using bytecode analysis.
 *
 * This task uses ASM (a bytecode manipulation framework) to scan compiled `.class` files
 * and detect which class extends or implements a specified super-class. It's primarily
 * used to automatically detect plugin main classes (e.g., classes extending `JavaPlugin`
 * for Spigot, `Plugin` for BungeeCord).
 *
 * ## How it works
 *
 * The task performs the following steps:
 *
 * 1. **Bytecode Scanning**: Scans all `.class` files in [classDirectories] using ASM's [ClassReader]
 *    - Uses optimized flags: `SKIP_CODE`, `SKIP_DEBUG`, `SKIP_FRAMES` for faster processing
 *    - Only extracts class metadata (name, superclass, access modifiers)
 *
 * 2. **Class Hierarchy Building**: For each class file, [SubclassDetector] visitor extracts:
 *    - Class name (e.g., `com/example/MyPlugin`)
 *    - Superclass name (e.g., `org/bukkit/plugin/java/JavaPlugin`)
 *    - Access modifiers (public, abstract, final, etc.)
 *
 * 3. **Detection Context**: All discovered classes are registered in [DetectionContext],
 *    which maintains a directed graph of class inheritance relationships
 *
 * 4. **Main Class Resolution**: Uses `DetectionContext.findMainClass(parentName)` to find
 *    a valid subclass by traversing the inheritance graph:
 *    - Must be a non-abstract, public class
 *    - Must directly or indirectly extend/implement [superClassName]
 *    - Prefers direct subclasses over deeper inheritance
 *
 * 5. **Result Writing**: The fully-qualified class name (in dot notation, e.g.,
 *    `com.example.MyPlugin`) is written to [outputFile]
 *
 * ## Incremental Processing
 *
 * This task supports Gradle's incremental build feature. It processes only changed
 * `.class` files and stops scanning early once a valid main class is found.
 *
 * ## Usage in Spigradle
 *
 * Spigradle automatically registers this task for each platform:
 * - Spigot: Detects subclasses of `org/bukkit/plugin/java/JavaPlugin`
 * - BungeeCord: Detects subclasses of `net/md_5/bungee/api/plugin/Plugin`
 * - NukkitX: Detects subclasses of `cn/nukkit/plugin/PluginBase`
 *
 * The detected main class is then used in `plugin.yml`/`bungee.yml` generation.
 *
 * ## Example Usage
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
 * Kotlin Example:
 *
 * ```kotlin
 * import io.typst.spigradle.SubclassDetection
 *
 * tasks {
 *   val findSubclass by registering(SubclassDetection::class) {
 *     superClassName.set("com.my.sample.SuperType")
 *     classDirectories.from(sourceSets["main"].output.classesDirs)
 *     outputFile.set(file("result.txt"))
 *   }
 * }
 * ```
 * @since 1.3.0
 */
abstract class SubclassDetection : DefaultTask() {
    init {
        group = "spigradle"
        description = "Detect the jvm subclass."
    }

    /**
     *  The map, superclass's full qualified class name to OutputFile.
     *  The output file will be written a plain text, the subclass's FQCN.
     */
    @get:OutputFiles
    abstract val outputFileBySuperclass: MapProperty<String, RegularFile>

    /**
     * The class directories used to target of the subclass detection.
     */
    @get:SkipWhenEmpty
    @get:InputFiles
    abstract val classDirectories: ConfigurableFileCollection

    @TaskAction
    fun inspect() {
        val contextR = AtomicReference(DetectionContext())
        val options = ClassReader.SKIP_CODE and ClassReader.SKIP_DEBUG and ClassReader.SKIP_FRAMES

        val outputFileBySuperclass = outputFileBySuperclass.get()
        var error = false

        for (file in classDirectories.asFileTree.files) {
            if (file.extension != "class" || !file.isFile) continue
            logger.log(LogLevel.DEBUG, "visit class: ${file.name}")
            try {
                file.inputStream().buffered().use {
                    ClassReader(it).accept(SubclassDetector(contextR), options)
                }
            } catch (ex: Exception) {
                project.logger.log(
                    LogLevel.WARN,
                    "[Spigradle] Error while reading class '${file.name}' bytecode",
                    ex
                )
                error = true
            }
            val ctx = contextR.get()
            if (outputFileBySuperclass.keys.all { ctx.findMainClass(it) != null }) {
                break
            }
        }
        if (error) {
            project.logger.log(
                LogLevel.LIFECYCLE,
                "[Spigradle] Some errors has occurred while reading class bytecode using ASM. Please update Spigradle. You can append `--debug` option in the $name task to see logs."
            )
        }

        val ctx = contextR.get()
        for ((superclassName, outputFile) in outputFileBySuperclass) {
            val subclassDef = ctx.findMainClass(superclassName)
            if (subclassDef != null) {
                outputFile.asFile.apply {
                    parentFile.mkdirs()
                }.writeText(subclassDef.name.replace("/", "."))
            } else {
                runCatching {
                    outputFile.asFile.delete()
                }
                logger.log(LogLevel.WARN, "Couldn't find a subclass extends $superclassName")
                logger.log(LogLevel.WARN, ctx.toString())
            }
        }
    }

    companion object {
        internal fun register(
            project: Project,
            taskName: String,
            outputFileBySuperclass: Provider<Map<String, RegularFile>>,
        ): TaskProvider<SubclassDetection> {
            return project.tasks.register(taskName, SubclassDetection::class) {
                project.pluginManager.withPlugin("java") {
                    val sourceSets = project.extensions.getByType(SourceSetContainer::class)
                    val mainSourceSet = sourceSets.named("main")
                    classDirectories.from(mainSourceSet.map {
                        it.output.classesDirs
                    })

                    dependsOn(mainSourceSet.map {
                        project.tasks.named(it.compileJavaTaskName)
                    })
                }
                this.outputFileBySuperclass.convention(outputFileBySuperclass)
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
        interfaces: Array<String>?,
    ) {
        val classDef = ClassDefinition.fromASM(access, name, superName, interfaces?.toSet() ?: emptySet())
        contextR.updateAndGet { ctx ->
            ctx.addClassDef(classDef)
        }
    }
}
