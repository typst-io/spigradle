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
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
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
abstract class YamlGenerate : DefaultTask() {
    init {
        group = "spigradle"
        description = "Generate the yaml file"
    }

    /**
     * The property map of yaml.
     */
    @get:Input
    abstract val properties: MapProperty<String, Any>

    /**
     * The encoding of the file.
     */
    @get:Input
    val encoding: Property<String> = project.objects.property<String>().convention("UTF-8")

    /**
     * The files that will be output.
     */
    @get:OutputFiles
    abstract val outputFiles: ConfigurableFileCollection

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

internal fun Project.getFallbackProperties(
    mapProperty: Provider<Map<String, Any?>>,
    fallbackPropertiesF: Provider<Map<String, RegularFile>>,
): Provider<Map<String, Any>> {
    return fallbackPropertiesF.map { fallbackProperties ->
        val map = mapProperty.get()
        val ret = map.toMutableMap()
        for ((key, file) in fallbackProperties) {
            if (map[key] != null) continue
            val fileText = runCatching {
                file.asFile.readText()
            }.getOrNull()
            if (fileText != null) {
                ret[key] = fileText
            }
        }
        // remove nulls
        ret.flatMap { (k, v) ->
            if (v != null) {
                listOf(k to v)
            } else emptyList()
        }.toMap()
    }
}

internal fun registerDescGenTask(
    project: Project,
    ctx: ModuleRegistrationContext,
): Pair<TaskProvider<SubclassDetection>, TaskProvider<YamlGenerate>> {
    val detectionTask =
        SubclassDetection.register(project, ctx.detectEntrypointsTaskName, ctx.getOutputFileBySuperclass())
    detectionTask.configure {
        group = ctx.platformName
    }
    val generationTask = project.tasks.register(ctx.generateDescriptionTaskName, YamlGenerate::class) {
        group = ctx.platformName
        inputs.files(ctx.getDetectionOutputFiles())
        properties.set(
            project.getFallbackProperties(
                ctx.descriptionProperties,
                ctx.getFileFallbackProperties()
            )
        )
        outputFiles.from(temporaryDir.resolve(ctx.descFileName))
        outputFiles.from(findResourceDirs(project, ctx.descFileName))

        doLast {
            val properties = properties.get()
            for (property in ctx.pluginDescriptionProperties.get()) {
                if (!property.mandatory) continue
                if (property.name !in properties) {
                    throw GradleException("The mandatory '${property.name}' property is not presented!")
                }
            }
        }
    }
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
    project.pluginManager.withPlugin("java") {
        val classes: Task by project.tasks
        classes.dependsOn(generationTask)
    }
    return detectionTask to generationTask
}

internal fun findResourceDirs(project: Project, fileName: String): List<File> {
    val files = mutableListOf<File>()
    project.pluginManager.withPlugin("java") {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class)
        files += listOf("main", "test").mapNotNull {
            sourceSets[it].output.resourcesDir
        }.map {
            File(it, fileName)
        }
    }
    return files
}