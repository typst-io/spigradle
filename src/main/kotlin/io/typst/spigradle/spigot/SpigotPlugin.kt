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

import io.typst.spigradle.*
import io.typst.spigradle.debug.DebugExtension
import io.typst.spigradle.debug.DebugRegistrationContext
import io.typst.spigradle.debug.DebugTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

/**
 * A Spigot plugin that provides:
 *
 * Extensions (configuration blocks, NOT tasks):
 * - spigot([SpigotExtension]): extension for the Spigot environment
 * - debugSpigot([DebugExtension]): extension for Spigot (Paper) debugging (configures `debug${ProjectName}` task)
 *     - jvmArgs: defaults to `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${jvmDebugPort}`
 *     - programArgs: defaults to `nogui`
 *
 * Plugins:
 * - io.typst.spigradle.base([io.typst.spigradle.SpigradlePlugin]): base plugin
 *
 * Required plugins:
 * - java
 * - org.jetbrains.gradle.plugin.idea-ext
 *
 * Tasks:
 * - generateSpigotDescription([io.typst.spigradle.YamlGenerate]): task to generate `plugin.yml`.
 * - detectSpigotMain([io.typst.spigradle.SubclassDetection]): task to detect the main class.
 *
 * Tasks for debugging:
 * - debug${project.name.caseKebabToPascal()}([Task][org.gradle.api.Task]): task to start the server in a new terminal window with the server platform (Paper). The debug directory is `$PROJECT_HOME/.gradle/spigradle-debug/${platform}`
 *     - dependsOn: downloadPaper, copyArtifactJar, createJavaDebugScript, `preparePluginDependencies`
 * - cleanDebug${project.name.caseKebabToPascal()}([Delete][org.gradle.api.tasks.Delete]): task to clean the project's debug directory
 * - cleanCachePaper([Delete][org.gradle.api.tasks.Delete]): task to clean the global cached `paper.jar` file
 *
 * Trivial tasks for debugging:
 * - preparePluginDependencies([PluginDependencyPrepareTask]): task to download the plugin dependencies
 * - copyArtifactJar([org.gradle.api.tasks.Copy]): task to copy the project artifact JAR, writes `eula.txt`.
 * - downloadPaper([PaperDownloadTask]): task to download the latest build of the version. The download path is `$GRADLE_USER_HOME/spigradle-debug-jars/$version/${platform}.jar`
 * - createJavaDebugScript([io.typst.spigradle.debug.CreateJavaDebugScriptTask]): writes a script file to run the server on Windows/Unix
 *
 * IDEA run configurations (NOTE: These are only generated if the plugin `org.jetbrains.gradle.plugin.idea-ext` applied):
 * - Debug$ProjectName: `Remote JVM Debug` configuration
 *     - port: [DebugExtension.jvmDebugPort]
 * - Run$ProjectName: `JAR Application` configuration that you can run or debug from the Run/Debug button UI
 *     - beforeRun: gradle tasks `downloadPaper`, `copyArtifactJar`, `createJavaDebugScript`, `preparePluginDependencies`
 *     - NOTE: You need to click the Refresh Gradle Project button in IDEA if you change the debugSpigot extension.
 */
class SpigotPlugin : Plugin<Project> {
    companion object {
        val platformName: String = "spigot"
        val genDescTask: String = "generate${platformName.capitalized()}Description"
        val mainDetectTask: String = "detect${platformName.capitalized()}Main"
        val SPIGOT_TYPE = PluginConvention(
            serverName = "spigot",
            descFile = "plugin.yml",
            mainSuperClass = "org/bukkit/plugin/java/JavaPlugin"
        )

        private fun getMinecraftMinimumJavaVersion(semVer: String): Int {
            val versions = semVer.split(".")
            val minor = versions[1].toInt()
            val fix = versions.getOrNull(2)?.toInt() ?: 0
            return if (minor <= 16) {
                // https://www.minecraftforum.net/forums/minecraft-java-edition/discussion/3041524-help-will-this-laptop-play-java
                8
            } else if (minor <= 17) {
                // https://www.minecraft.net/en-us/article/minecraft-snapshot-21w19a
                16
            } else if (minor <= 20 && fix <= 4) {
                // https://www.minecraft.net/en-us/article/minecraft-1-18-pre-release-2
                17
            } else {
                // https://www.minecraft.net/en-us/article/minecraft-snapshot-24w14a
                21
            }
        }
    }

        fun createModuleRegistrationContext(
            project: Project,
            extension: SpigotExtension,
        ): ModuleRegistrationContext<SpigotExtension> {
            return ModuleRegistrationContext(
                platformName,
                "plugin.yml",
                extension,
                project.getMainDetectOutputFile(platformName),
                genDescTask,
                mainDetectTask,
                "org/bukkit/plugin/java/JavaPlugin"
            )
        }
    }

    override fun apply(project: Project) {
        // apply base
        project.pluginManager.apply(SpigradlePlugin::class)

        // register tasks
        val extension = project.extensions.create(platformName, SpigotExtension::class)
        val ctx = createModuleRegistrationContext(project, extension)
        registerDescGenTask(project, ctx) { desc ->
            desc.toMap()
        }
        setupSpigotDebug(project, extension)
    }

    private fun setupSpigotDebug(project: Project, extension: SpigotExtension) {
        val paperExt = project.extensions.create("debugSpigot", DebugExtension::class.java).apply {
            jvmArgs.convention(jvmDebugPort.map { port ->
                listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${port}")
            })
            programArgs.convention(listOf("nogui"))
        }
        val jarTask = if (project.hasJavaPlugin) {
            project.tasks.named("jar", Jar::class.java)
        } else null
        val ctx = DebugRegistrationContext(
            "paper",
            paperExt.version,
            "",
            "plugins",
            jarTask,
            paperExt.jvmArgs,
            paperExt.programArgs,
            paperExt.jvmDebugPort,
            paperExt.javaHome.map { it.file("bin/java") },
            false,
            paperExt.eula
        )
        val downloadPaper = project.tasks.register("downloadPaper", PaperDownloadTask::class.java) {
            group = ctx.taskGroupName

            version.set(paperExt.version)
            outputFile.set(ctx.getDownloadOutputFile(project))
        }
        val preparePluginDependencies =
            project.tasks.register("preparePluginDependencies", PluginDependencyPrepareTask::class.java) {
                group = ctx.taskGroupName

                pluginNames.set(paperExt.downloadSoftDepends.map {
                    if (it) {
                        (extension.depend.orNull ?: emptyList()) + (extension.softDepend.orNull ?: emptyList())
                    } else extension.depend.orNull ?: emptyList()
                })
                downloadSoftDepends.set(paperExt.downloadSoftDepends)
                outputDir.set(ctx.getDebugArtifactDir(project))
                // for up-to-date check
                inputs.property("javaVersion", paperExt.version)

                doFirst {
                    // NOTE: minJavaVer required consistent maintenance
                    // REFERENCE: https://docs.papermc.io/paper/getting-started/#requirements
                    val minimumJavaVersion = getMinecraftMinimumJavaVersion(paperExt.version.get())
                    val javaVersion = paperExt.javaVersion.orNull?.asInt()
                    if (javaVersion != null && javaVersion < minimumJavaVersion) {
                        // https://docs.gradle.org/current/userguide/reporting_problems.html
                        throw GradleException("Paper ${paperExt.version.get()} requires at least Java ${minimumJavaVersion}! Please set the `java.toolchain.languageVersion`, or `debugSpigot.javaVersion` to JavaLanguageVersion(21), or kotlin.jvmToolchain(21) if Kotlin!")
                    }
                }
            }
        DebugTask.register(
            project,
            ctx.copy(downloadTask = downloadPaper, extraTasks = listOf(preparePluginDependencies))
        )
    }
}
