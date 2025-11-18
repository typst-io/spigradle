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

package io.typst.spigradle.spigot

import io.typst.spigradle.PluginConvention
import io.typst.spigradle.applySpigradlePlugin
import io.typst.spigradle.debug.DebugExtension
import io.typst.spigradle.debug.DebugRegistrationContext
import io.typst.spigradle.debug.DebugTask
import io.typst.spigradle.groovyExtension
import io.typst.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.gradle.ext.IdeaExtPlugin

/**
 * The Spigot plugin that adds:
 *
 * Extensions:
 * - spigot([SpigotExtension]): extension for spigot environment
 * - debugSpigot([DebugExtension]): extension for spigot(paper) debug
 *     - jvmArgs: defaults to `-agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address=${project.name}`
 *     - programArgs: defaults to `nogui`
 *
 * Plugins:
 * - `io.typst.spigradle.base`([SpigradlePlugin][io.typst.spigradle.SpigradlePlugin]): base plugin
 * - `org.jetbrains.gradle.plugin.idea-ext`([IdeaExtPlugin]): idea ext plugin for generating the `Run Configuration`
 *
 * Tasks:
 * - generatePluginYaml([YamlGenerate][io.typst.spigradle.YamlGenerate]): task for the 'plugin.yml' generation.
 * - detectSpigotMain([SubclassDetection][io.typst.spigradle.SubclassDetection]): task for the main-class detection.
 *
 * Tasks for debug:
 * - `debug${project.name.caseKebabToPascal()}`([Task][org.gradle.api.Task]): task for the debug jar with the server platform(paper). the debug dir is `$PROJECT_HOME/.gradle/spigradle-debug/${platform}`
 * - `cleanDebug${project.name.caseKebabToPascal()}`([Delete][org.gradle.api.tasks.Delete]): task to clean the project's debug dir
 * - `cleanCache${platformName}`([Delete][org.gradle.api.tasks.Delete]): task to clean the global cache paper.jar
 *
 * Trivial tasks for debug:
 * - `preparePluginDependencies`([PluginDependencyPrepareTask]): task for download the plugin dependencies.
 * - `copyArtifactJar`([Copy][org.gradle.api.tasks.Copy]): task to copy the project artifact jar
 * - `downloadPaper`([PaperDownloadTask]): task to download latest build of the version. the download path is `$GRADLE_USER_HOME/spigradle-debug-jars/$version/${platform}.jar
 */
class SpigotPlugin : Plugin<Project> {
    companion object {
        val SPIGOT_TYPE = PluginConvention(
            serverName = "spigot",
            descFile = "plugin.yml",
            mainSuperClass = "org/bukkit/plugin/java/JavaPlugin"
        )
    }

    private val Project.spigot get() = extensions.getByName<SpigotExtension>(SPIGOT_TYPE.descExtension)

    override fun apply(project: Project) {
        with(project) {
            applySpigradlePlugin()
            // TODO: auto libraries
            registerDescGenTask(SPIGOT_TYPE, SpigotExtension::class.java) { desc ->
                desc.encodeToMap()
            }
            setupGroovyExtensions()

            // debug
            project.rootProject.pluginManager.apply(IdeaExtPlugin::class.java)
            setupSpigotDebug()
        }
    }

    private fun Project.setupSpigotDebug() {
        val paperExt = extensions.create("debugSpigot", DebugExtension::class.java).apply {
            jvmArgs.convention(provider {
                listOf("-agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address=${project.name}")
            })
            programArgs.convention(listOf("nogui"))
        }
        val ctx = DebugRegistrationContext(
            "paper",
            paperExt.version,
            "",
            "plugins",
            project.tasks.named("jar", Jar::class.java),
            paperExt.jvmArgs,
            paperExt.programArgs,
            false,
            paperExt.eula
        )
        val downloadPaper = tasks.register("downloadPaper", PaperDownloadTask::class.java) {
            group = ctx.taskGroupName

            dependsOn(ctx.jarTask)
            version.set(paperExt.version)
            outputFile.set(ctx.getDownloadOutputFile(this@setupSpigotDebug))
        }
        val preparePluginDependencies =
            tasks.register("preparePluginDependencies", PluginDependencyPrepareTask::class.java) {
                group = ctx.taskGroupName

                pluginNames.set(paperExt.downloadSoftDepends.map {
                    if (it) {
                        spigot.depends + spigot.softDepends
                    } else spigot.depends
                })
                outputDir.set(ctx.getDebugArtifactDir(this@setupSpigotDebug))
            }
        DebugTask.register(this, ctx).configure {
            dependsOn(downloadPaper, preparePluginDependencies)
        }
    }

    private fun Project.setupGroovyExtensions() {
        val spigotExt = spigot.groovyExtension
        // literal
        spigotExt.set("POST_WORLD", Load.POST_WORLD)
        spigotExt.set("POSTWORLD", Load.POST_WORLD)
        spigotExt.set("STARTUP", Load.STARTUP)
    }
}
