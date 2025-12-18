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
import io.typst.spigradle.spigot.SpigotBasePlugin.Companion.PLATFORM_NAME
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar

/**
 * The Spigot plugin that provides:
 *
 * Applies plugins:
 * - java-base([JavaBasePlugin])
 * - io.typst.spigradle.spigot-base([SpigotBasePlugin])
 *
 * Optional plugins:
 * - java: You must manually apply it if you develop a pure java project.
 * - org.jetbrains.gradle.plugin.idea-ext (recommended for IntelliJ IDEA) **Important:** Only applies on the root project
 *
 * Dependency configurations:
 * - compileOnlySpigot: Compile only dependencies that will be exported to plugin.yml libraries.
 * - spigotLibrariesClasspath: Resolvable view of compileOnlySpigot for generating plugin.yml libraries.
 *
 * Tasks:
 * - generateSpigotDescription([io.typst.spigradle.YamlGenerate]): task to generate `plugin.yml`.
 * - detectSpigotMain([io.typst.spigradle.SubclassDetection]): task to detect the main class.
 *
 * Tasks for debugging:
 * - debugProjectName([Task][org.gradle.api.Task]): task to start the server in a new terminal window with the server platform (Paper). The debug directory is `$PROJECT_HOME/.gradle/spigradle-debug/${platform}`
 *   - dependsOn: prepareProjectName`
 * - cleanDebugProjectName([Delete][org.gradle.api.tasks.Delete]): task to clean the project's debug directory
 * - cleanCachePaper([Delete][org.gradle.api.tasks.Delete]): task to clean the global cached `paper.jar` file
 *
 * Trivial tasks for debugging:
 * - preparePluginDependencies([PluginDependencyPrepareTask]): task to download the plugin dependencies
 * - copyArtifactJar([org.gradle.api.tasks.Copy]): task to copy the project artifact JAR, writes `eula.txt`.
 * - downloadPaper([PaperDownloadTask]): task to download the latest build of the version. The download path is `$GRADLE_USER_HOME/spigradle-debug-jars/$version/${platform}.jar`
 * - createJavaDebugScript([io.typst.spigradle.debug.CreateJavaDebugScriptTask]): writes a script file to run the server on Windows/Unix
 * - prepareProjectName([org.gradle.api.Task]): A lifecycle task to prepare debugs
 *   - dependsOn: `downloadPaper`, `copyArtifactJar`, `createJavaDebugScript`, `preparePluginDependencies`
 *
 * IDEA run configurations (NOTE: These are only generated if the plugin `org.jetbrains.gradle.plugin.idea-ext` applied):
 * - Debug$ProjectName: `Remote JVM Debug` configuration
 *     - port: [DebugExtension.jvmDebugPort]
 * - Run$ProjectName: `JAR Application` configuration that you can run or debug from the Run/Debug button UI
 *     - beforeRun: Gradle task `prepareProjectName`
 *     - NOTE: You need to click the Refresh Gradle Project button in IDEA if you change the debugSpigot extension.
 */
class SpigotPlugin : Plugin<Project> {
    companion object {
        @JvmStatic
        val GENERATE_DESCRIPTION_TASK_NAME: String = "generate${PLATFORM_NAME.asCamelCase(true)}Description"

        @JvmStatic
        val DETECT_MAIN_TASK_NAME: String = "detect${PLATFORM_NAME.asCamelCase(true)}Main"

        internal fun getMinecraftMinimumJavaVersion(semVer: String): Int {
            val versions = semVer.split(".")
            val minor = versions[1].toInt()
            val fix = versions.getOrNull(2)?.toInt() ?: 0
            // NOTE: required consistent maintenance
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

        internal fun createModuleRegistrationContext(
            project: Project,
            extension: SpigotExtension,
        ): ModuleRegistrationContext<SpigotExtension> {
            return ModuleRegistrationContext(
                PLATFORM_NAME,
                "plugin.yml",
                extension,
                project.getMainDetectOutputFile(PLATFORM_NAME),
                GENERATE_DESCRIPTION_TASK_NAME,
                DETECT_MAIN_TASK_NAME,
                "org/bukkit/plugin/java/JavaPlugin"
            )
        }
    }

    override fun apply(project: Project) {
        // apply base plugins
        project.pluginManager.apply(JavaBasePlugin::class.java)
        project.pluginManager.apply(SpigotBasePlugin::class.java)

        // register dependency configuration
        val pluginLibsProp = if (project.hasJavaPlugin) {
            // register configuration: https://docs.gradle.org/current/userguide/declaring_configurations.html#sec:defining-custom-configurations
            val compileOnlySpigot = project.configurations.create("compileOnlySpigot").apply {
                isCanBeConsumed = false
                isCanBeResolved = false
                description = "Compile only dependencies that will be exported to plugin.yml libraries."
            }
            project.configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).configure {
                extendsFrom(compileOnlySpigot)
            }
            val spigotLibrariesClasspath = project.configurations.create("spigotLibrariesClasspath").apply {
                isCanBeConsumed = false
                isCanBeResolved = true
                extendsFrom(compileOnlySpigot)
                description = "Resolvable view of compileOnlySpigot for generating plugin.yml libraries."

                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_API))
                }
            }
            project.provider {
                spigotLibrariesClasspath.incoming.resolutionResult.root
                    .dependencies
                    .asSequence()
                    .mapNotNull {
                        if (it is ResolvedDependencyResult && !it.isConstraint) {
                            it.selected.id as? ModuleComponentIdentifier
                        } else null
                    }
                    .map { id ->
                        "${id.group}:${id.module}:${id.version}"
                    }
                    .distinct()
                    .sorted()
                    .toList()
            }
        } else null

        // configure extension
        val extension = project.extensions.getByType(SpigotExtension::class.java)
        if (pluginLibsProp != null) {
            extension.libraries.convention(pluginLibsProp)
        }

        // configure tasks
        val ctx = createModuleRegistrationContext(project, extension)
        registerDescGenTask(project, ctx) { desc ->
            desc.toMap()
        }
        setupSpigotDebug(project, extension)
    }

    private fun setupSpigotDebug(project: Project, extension: SpigotExtension) {
        val paperDebugExt = project.extensions.getByType(DebugExtension::class.java).apply {
            jvmArgs.convention(jvmDebugPort.map { port ->
                listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${port}")
            })
            programArgs.convention(listOf("nogui"))
        }
        val jarTask = if (project.hasJavaPlugin) {
            paperDebugExt.projectJarTask.convention(project.tasks.named("jar", Jar::class.java))
        } else null
        val ctx = DebugRegistrationContext(
            "paper",
            paperDebugExt.version,
            "",
            "plugins",
            jarTask,
            paperDebugExt.jvmArgs,
            paperDebugExt.programArgs,
            paperDebugExt.jvmDebugPort,
            paperDebugExt.javaHome.map { it.file("bin/java") },
            false,
            paperDebugExt.eula
        )
        val downloadPaper = project.tasks.register("downloadPaper", PaperDownloadTask::class.java) {
            group = ctx.taskGroupName

            version.set(paperDebugExt.version)
            outputFile.set(ctx.getDownloadOutputFile(project))
        }
        val preparePluginDependencies =
            project.tasks.register("preparePluginDependencies", PluginDependencyPrepareTask::class.java) {
                group = ctx.taskGroupName

                pluginNames.set(paperDebugExt.downloadSoftDepend.map {
                    if (it) {
                        (extension.depend.orNull ?: emptyList()) + (extension.softDepend.orNull ?: emptyList())
                    } else extension.depend.orNull ?: emptyList()
                })
                downloadSoftDepend.set(paperDebugExt.downloadSoftDepend)
                outputDir.set(ctx.getDebugArtifactDir(project))
                // for up-to-date check
                inputs.property("javaVersion", paperDebugExt.version)

                project.pluginManager.withPlugin("java") {
                    resolvableConfigurations.convention(
                        project.configurations.named("compileClasspath").map(::setOf)
                    )
                }

                doFirst {
                    // NOTE: minJavaVer required consistent maintenance
                    // REFERENCE: https://docs.papermc.io/paper/getting-started/#requirements
                    val minimumJavaVersion = getMinecraftMinimumJavaVersion(paperDebugExt.version.get())
                    val javaVersion = paperDebugExt.javaVersion.orNull?.asInt()
                    if (javaVersion != null && javaVersion < minimumJavaVersion) {
                        // https://docs.gradle.org/current/userguide/reporting_problems.html
                        throw GradleException("Paper ${paperDebugExt.version.get()} requires at least Java ${minimumJavaVersion}! Please set the `java.toolchain.languageVersion`, or `debugSpigot.javaVersion` to JavaLanguageVersion(21), or kotlin.jvmToolchain(21) if Kotlin!")
                    }
                }
            }
        DebugTask.register(
            project,
            ctx.copy(downloadTask = downloadPaper, extraTasks = listOf(preparePluginDependencies))
        )
    }
}
