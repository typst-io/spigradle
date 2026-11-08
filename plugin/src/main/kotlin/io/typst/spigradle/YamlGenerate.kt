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
import org.gradle.api.provider.SetProperty
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
     * The properties explicitly configured for the YAML, excluding values from [fallbackFiles].
     * Use [readProperties] to get the properties merged with the fallbacks.
     */
    @get:Input
    abstract val properties: MapProperty<String, Any>

    @get:Input
    abstract val mandatoryProperties: SetProperty<String>

    /**
     * The property name to file map, the file content is used when the property is absent.
     */
    @get:Internal
    abstract val fallbackFiles: MapProperty<String, RegularFile>

    // a map can't be @InputFiles, so track the files separately
    @get:InputFiles
    val fallbackFileInputs: Provider<Collection<RegularFile>>
        get() = fallbackFiles.map { it.values }

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

    fun readProperties(): Map<String, Any> {
        val ret = mutableMapOf<String, Any>()
        ret.putAll(properties.get())
        for ((key, file) in fallbackFiles.get()) {
            if (ret[key] != null) continue
            val fileText = runCatching {
                file.asFile.readText()
            }.getOrNull()
            if (fileText != null) {
                ret[key] = fileText
            }
        }
        return ret
    }

    @TaskAction
    fun generate() {
        val properties = readProperties()
        // validate
        for (key in mandatoryProperties.get()) {
            if (properties[key] == null) {
                throw GradleException("The mandatory '${key}' property is not presented!")
            }
        }

        val dumpSettings = DumpSettings.builder()
            .setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setBestLineBreak("\n")
            .setDereferenceAliases(true)
            .build()
        val dump = Dump(dumpSettings)
        val yaml = dump.dumpToString(properties)
        outputFiles.forEach { file ->
            file.parentFile?.mkdirs()
            file.bufferedWriter(Charset.forName(encoding.get())).use {
                it.write(yaml)
            }
        }
    }

    companion object {
        fun getGeneratedResourceDir(platformName: String): String =
            "generated/spigradle/${platformName}/resources"
    }
}

internal fun YamlGenerate.setDescriptionProperties(ctx: ModuleRegistrationContext) {
    properties.set(ctx.descriptionProperties.map { props ->
        props.flatMap {
            if (it.value != null) {
                listOf(it.key to it.value as Any)
            } else emptyList()
        }.toMap()
    })
    mandatoryProperties.set(ctx.pluginDescriptionProperties.map { props ->
        props.filter { it.mandatory }.map { it.name }
    })
    fallbackFiles.set(ctx.getFileFallbackProperties())
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
    val generatedResourceDir = project.layout.buildDirectory.dir(YamlGenerate.getGeneratedResourceDir(ctx.platformName))
    val generationTask = project.tasks.register(ctx.generateDescriptionTaskName, YamlGenerate::class) {
        group = ctx.platformName
        inputs.files(ctx.getDetectionOutputFiles())
        setDescriptionProperties(ctx)
        outputFiles.from(generatedResourceDir.map { it.file(ctx.descFileName) })
    }
    // Task ordering part, YamlGenerate `depends` on detection, the resource dir `builtBy` generationTask
    generationTask.configure { dependsOn(detectionTask) }
    project.pluginManager.withPlugin("java") {
        val sourcesSets = project.extensions.getByType(SourceSetContainer::class)
        sourcesSets.named("main") {
            resources.srcDir(project.files(generatedResourceDir).builtBy(generationTask))
        }
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