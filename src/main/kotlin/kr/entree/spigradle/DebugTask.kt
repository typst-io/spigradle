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

package kr.entree.spigradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.useToRun
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.JarApplication
import org.jetbrains.gradle.ext.Remote
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File
import java.io.InputStreamReader
import java.util.jar.JarFile

private data class PluginData(val description: Map<String, Any?>, val file: File)
private data class PluginDependency(val requires: Set<String> = emptySet(), val options: Set<String> = emptySet()) {
    val all get() = requires + options
}

private fun findDependencies(pluginName: String, dataMap: Map<String, PluginData>): PluginDependency {
    val data = dataMap[pluginName] ?: return PluginDependency()
    val requires = mutableSetOf<String>()
    val options = mutableSetOf<String>()
    val (desc, _) = data
    listOf("depend", "softdepend").forEach { key ->
        val depends = (desc[key] as? Collection<*>)?.map { it.toString() } ?: emptyList()
        val target = if (key == "depend") requires else options
        depends.forEach {
            val deep = findDependencies(it, dataMap)
            requires += deep.requires
            options += deep.options
            target += it
        }
    }
    return PluginDependency(requires, options)
}

@Suppress("UNCHECKED_CAST")
internal fun readYamlDescription(file: File, configFileName: String): Map<String, Any?> {
    return runCatching {
        JarFile(file).use { jarFile ->
            val entry = jarFile.getEntry(configFileName) ?: return@use null
            jarFile.getInputStream(entry).use { jin ->
                val load = Load(LoadSettings.builder().build())
                load.loadFromReader(InputStreamReader(jin)) as? Map<String, Any?>
            }
        }
    }.getOrNull() ?: emptyMap()
}

internal fun Project.createRunConfigurations(name: String, debug: CommonDebug) {
    val idea: IdeaModel by extensions
    idea.project?.settings {
        runConfigurations {
            register("Remote$name", Remote::class) {
                host = "localhost"
                port = debug.agentPort
            }
            register("Run$name", JarApplication::class) {
                jarPath = debug.serverJar.absolutePath
                workingDirectory = debug.serverDirectory.absolutePath
                programParameters = debug.args.joinToString(" ")
                jvmArgs = debug.jvmArgs.joinToString(" ")
                beforeRun {
                    register("prepareServer", GradleTask::class) {
                        task = tasks.getByName("prepare$name")
                    }
                    register("preparePlugins", GradleTask::class) {
                        task = tasks.findByName("prepare${name}Plugins")
                            ?: tasks.getByName("prepare${name}Plugin") // TODO: Remove in 3.0
                    }
                }
            }
        }
    }
}

object DebugTask {
    fun Project.registerRunServer(serverName: String, debug: CommonDebug): TaskProvider<JavaExec> {
        return tasks.register("run$serverName", JavaExec::class) {
            standardInput = System.`in`
            logging.captureStandardOutput(LogLevel.LIFECYCLE)
            jvmArgs(lazyString { "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=${debug.agentPort}" })
            jvmArgs(debug.jvmArgs)
            args(debug.args)
            doFirst {
                if (!debug.serverJar.isFile) {
                    throw GradleException(
                        """
                        Cannot find the server jar at the given path: ${debug.serverJar.absolutePath}
                        Did you run 'debug$serverName' task instead of 'run$serverName'?
                    """.trimIndent()
                    )
                }
            }
        }
    }

    fun Project.registerPreparePlugins(
        taskName: String,
        descFileName: String,
        dependPlugins: Provider<Iterable<String>>,
    ): TaskProvider<Copy> {
        return tasks.register(taskName, Copy::class) {
            description = "Copy the plugin jars"

            // Ensure this task runs after all tasks producing potential plugin jars
            rootProject.allprojects.forEach { project ->
                mustRunAfter(project.tasks.withType<Jar>())
            }

            from(provider { tasks.findArtifactJar() ?: files() })
            from(provider {
                val pluginDataMap = (rootProject.allprojects.mapNotNull {
                    it.tasks.findArtifactJar()
                } + rootProject.allprojects.flatMap {
                    it.extensions.findByType<JavaPluginExtension>()?.run {
                        sourceSets["main"].run { runtimeClasspath + compileClasspath }
                    } ?: emptyList()
                }).asSequence().mapNotNull { depFile ->
                    val desc = readYamlDescription(depFile, descFileName)
                    if (desc.isNotEmpty() && desc["name"] != null)
                        PluginData(desc, depFile)
                    else null
                }.groupingBy { (desc, _) ->
                    desc["name"].toString()
                }.reduce { _, acc, element ->
                    if (element.file.length() > acc.file.length()) element else acc
                }
                val needPlugins = dependPlugins.get().fold(PluginDependency()) { acc, depName ->
                    val deepDep = findDependencies(depName, pluginDataMap)
                    PluginDependency(
                        acc.requires + deepDep.requires + depName,
                        acc.options + deepDep.options
                    )
                }
                val unresolved = needPlugins.requires.filter { pluginDataMap[it]?.file?.isFile != true }
                if (unresolved.isNotEmpty()) {
                    logger.error("Unable to resolve the plugin dependencies: [${unresolved.joinToString()}]")
                }
                needPlugins.all.mapNotNull {
                    pluginDataMap[it]?.file
                }
            })
        }
    }
}