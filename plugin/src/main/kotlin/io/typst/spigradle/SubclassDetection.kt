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
 * 4. **Main Class Resolution**: Uses `DetectionContext.findMainClassCandidates(parentName)` to find
 *    every candidate by traversing the inheritance graph:
 *    - Must be a non-abstract, public class
 *    - Must directly or indirectly extend/implement the superclass
 *
 * 5. **Result Writing**: When there is exactly one candidate, its fully-qualified class name (in dot notation,
 *    e.g., `com.example.MyPlugin`) is written to the output file of the superclass in [outputFileBySuperclass].
 *    Otherwise, the output file is deleted and a warning is logged, listing the candidates if there are many,
 *    so the main class has to be set explicitly.
 *
 * ## Up-to-date checks
 *
 * Gradle skips this task while the class files are unchanged. When it runs, it reads the header of every
 * class file; it doesn't process only the changed files.
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
 *   classDirectories.from sourceSets.main.output.classesDirs
 *   outputFileBySuperclass.put('com/my/sample/SuperType', layout.buildDirectory.file('result.txt'))
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
 *     classDirectories.from(sourceSets["main"].output.classesDirs)
 *     outputFileBySuperclass.put("com/my/sample/SuperType", layout.buildDirectory.file("result.txt"))
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
     * The map, the internal name of a superclass (e.g. `org/bukkit/plugin/java/JavaPlugin`) to its output file.
     * The output file will be written a plain text, the FQCN of the detected subclass.
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
        var ctx = DetectionContext()
        val options = ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES

        val outputFileBySuperclass = outputFileBySuperclass.get()
        var error = false

        for (file in classDirectories.asFileTree.files) {
            if (file.extension != "class" || !file.isFile) continue
            logger.log(LogLevel.DEBUG, "visit class: ${file.name}")
            try {
                file.inputStream().buffered().use {
                    ClassReader(it).accept(SubclassDetector { classDef -> ctx = ctx.addClassDef(classDef) }, options)
                }
            } catch (ex: Exception) {
                logger.log(
                    LogLevel.WARN,
                    "[Spigradle] Error while reading class '${file.name}' bytecode",
                    ex
                )
                error = true
            }
        }
        if (error) {
            logger.log(
                LogLevel.LIFECYCLE,
                "[Spigradle] Some errors has occurred while reading class bytecode using ASM. Please update Spigradle. You can append `--debug` option in the $name task to see logs."
            )
        }

        for ((superclassName, outputFile) in outputFileBySuperclass) {
            val candidates = ctx.findMainClassCandidates(superclassName)
            val file = outputFile.asFile
            if (candidates.size == 1) {
                file.apply {
                    parentFile.mkdirs()
                }.writeText(candidates[0].name.replace("/", "."))
                continue
            }
            runCatching {
                file.delete()
            }
            if (candidates.size >= 2) {
                logger.warn(
                    "[Spigradle] Found multiple candidates of the main class extending $superclassName: " +
                            "${candidates.joinToString { it.name.replace('/', '.') }}. " +
                            "Set the `main` property explicitly."
                )
            } else {
                logger.warn("[Spigradle] Couldn't find a public concrete class extending $superclassName")
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
    private val onClass: (ClassDefinition) -> Unit,
) : ClassVisitor(Opcodes.ASM9) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?,
    ) {
        onClass(ClassDefinition.fromASM(access, name, superName, interfaces?.toSet() ?: emptySet()))
    }
}
