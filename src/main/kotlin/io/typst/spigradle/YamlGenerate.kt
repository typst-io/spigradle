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

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import java.io.File
import java.nio.charset.Charset

/**
 * Generates a YAML into the given files.
 *
 * Groovy Example:
 *
 * ```groovy
 * import io.typst.spigradle.YamlGenerate
 *
 * task generateYaml(type: YamlGenerate) {
 *   properties.put('someProperty', 'AnyTypeOfValue')
 *   encoding = 'UTF-16'
 *   outputFiles.from file('result.yml')
 * }
 * ```
 *
 * Kotlin Example:
 *
 * ```kotlin
 * import io.typst.spigradle.YamlGenerate
 *
 * tasks {
 *   val generateYaml by registering(YamlGenerate) {
 *     properties.put("someProperty", "AnyTypeOfValue")
 *     encoding.set("UTF-16")
 *     outputFiles.from(file("result.yml"))
 *   }
 * }
 * ```
 *
 * @since 1.3.0
 */
open class YamlGenerate : DefaultTask() {
    init {
        group = "spigradle"
        description = "Generate the yaml file"
    }

    /**
     * The property map of yaml.
     */
    @Input
    val properties: MapProperty<String, Any> = project.objects.mapProperty(String::class, Any::class)

    /**
     * The encoding of the file.
     */
    @Input
    val encoding: Property<String> = project.objects.property<String>().convention("UTF-8")

    /**
     * The files that will be output.
     */
    @OutputFiles
    val outputFiles: ConfigurableFileCollection = project.objects.fileCollection()

    @TaskAction
    fun generate() {
        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setBestLineBreak("\n")
            .setDereferenceAliases(true)
            .build()
        val dump = Dump(dumpSettings)
        val yaml = dump.dumpToString(properties.get())
        outputFiles.forEach { file ->
            file.bufferedWriter(Charset.forName(encoding.get())).use {
                it.write(yaml)
            }
        }
    }
}

internal fun Project.getMainDetectivePropertiesProvider(
    map: Map<String, Any?>,
    detectResultFile: File,
): Provider<Map<String, Any?>> {
    return provider {
        if (!map.containsKey("main")) {
            val detectResult = runCatching {
                detectResultFile.readText()
            }.getOrNull() ?: runCatching {
                detectResultFile.parentFile.resolve("plugin_main").readText()
            }.getOrNull()
            val newMap = LinkedHashMap<String, Any?>()
            if (detectResult != null) {
                newMap.put("main", detectResult)
            }
            newMap.putAll(map)
            newMap as Map<String, Any?>
        } else {
            map
        }
    }
}

// TODO: Too complex! Should whole refactor in 3.0
internal fun <T> Project.registerDescGenTask(
    type: PluginConvention, extensionClass: Class<T>, serializer: (T) -> Map<String, Any?>,
) {
    val detectResultFile = getPluginMainPathFile(type.serverName)
    val description = extensions.create(type.descExtension, extensionClass, this)
    val detectionTask = SubclassDetection.register(this, type.mainDetectTask, type.serverName).applyToConfigure {
        group = type.taskGroup
        superClassName.set(type.mainSuperClass)
        outputFile.set(detectResultFile)
    }
    val generationTask = registerYamlGenTask(type).applyToConfigure {
        inputs.files(detectResultFile)
        group = type.taskGroup
        properties.set(getMainDetectivePropertiesProvider(serializer(description), detectResultFile))
        doFirst {
            notNull(properties.get()["main"]) {
                Messages.noMainFound(
                    type.descExtension,
                    type.descGenTask
                )
            }
        }
    }
    val classes: Task by tasks
    /*
    NOTE: Task ordering part
    https://docs.gradle.org/current/userguide/java_plugin.html

    compileJava       dependsOn: all tasks which contribute to the compilation classpath
    processResources
    *classes          dependsOn: compileJava, processResources
    jar               dependsOn: classes
    *assemble         dependsOn: jar
    *build            dependsOn: assemble

    detectionTask     dependsOn: classes
    generateTask      dependsOn: detectionTask
    *assemble         dependsOn: +generateTask

    Our generate task is part of compilation, thus depends by `classes` which describes compilation.
    Expected ordering: compileJava, ... -> detectionTask -> generateTask -> classes
     */
    generationTask.configure { dependsOn(detectionTask) }
    classes.dependsOn(generationTask)
}

internal fun Project.findResourceDirs(fileName: String): List<File> {
    val sourceSets = project.extensions.getByType(SourceSetContainer::class)
    return listOf("main", "test").mapNotNull {
        sourceSets[it].output.resourcesDir
    }.map {
        File(it, fileName)
    }
}

internal fun Project.registerYamlGenTask(taskName: String, fileName: String): TaskProvider<YamlGenerate> {
    return project.tasks.register(taskName, YamlGenerate::class) {
        outputFiles.from(temporaryDir.resolve(fileName))
        outputFiles.from(findResourceDirs(fileName))
    }
}

internal fun Project.registerYamlGenTask(type: PluginConvention): TaskProvider<YamlGenerate> =
    registerYamlGenTask(type.descGenTask, type.descFile)
