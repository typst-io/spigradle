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
import io.typst.spigradle.paper.PaperDebugRegistrationContext
import io.typst.spigradle.paper.PaperDebugTask
import io.typst.spigradle.spigot.SpigotBasePlugin.Companion.PLATFORM_NAME
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
        internal val spec: PlatformPluginSpec = PlatformPluginSpec(
            PLATFORM_NAME,
            "plugin.yml",
            listOf(
                PluginDescriptionProperty("main", "org/bukkit/plugin/java/JavaPlugin", mandatory = true)
            )
        )

        @JvmStatic
        val GENERATE_PLUGIN_DESCRIPTION_TASK_NAME: String = spec.generateDescriptionTaskName

        @JvmStatic
        val DETECT_ENTRYPOINTS_TASK_NAME: String = spec.detectEntrypointsTaskName

        @JvmStatic
        val COMPILE_ONLY_SPIGOT_CONFIGURATION_NAME: String = "compileOnlySpigot"

        @JvmStatic
        val SPIGOT_LIBRARIES_CLASSPATH_CONFIGURATION_NAME: String = "spigotLibrariesClasspath"

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

        internal fun createModuleRegistrationContext(project: Project): ModuleRegistrationContext {
            val extension = project.extensions.getByType(SpigotExtension::class.java)
            return spec.createModuleRegistrationContext(project, project.provider { extension.toMap() })
        }

        internal fun createPaperDebugRegistrationContext(
            project: Project,
        ): PaperDebugRegistrationContext {
            val extension = project.extensions.getByType(SpigotExtension::class.java)
            val debugExtension = project.extensions.getByType(DebugExtension::class.java)
            val jarTask = if (project.hasJavaPlugin) {
                debugExtension.jarTask.convention(project.tasks.named("jar", Jar::class.java))
            } else null
            val subCtx = DebugRegistrationContext(
                PLATFORM_NAME,
                extension.version,
                "",
                "plugins",
                jarTask,
                debugExtension.jvmArgs,
                debugExtension.programArgs,
                debugExtension.jvmDebugPort,
                debugExtension.javaHome.map { it.file("bin/java") },
                false,
                debugExtension.eula
            )
            return PaperDebugRegistrationContext(
                subCtx,
                extension.depend,
                extension.softDepend
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
            val compileOnlySpigot = project.configurations.create(COMPILE_ONLY_SPIGOT_CONFIGURATION_NAME).apply {
                isCanBeConsumed = false
                isCanBeResolved = false
                description = "Compile only dependencies that will be exported to plugin.yml libraries."
            }
            project.configurations.named(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).configure {
                extendsFrom(compileOnlySpigot)
            }
            val spigotLibrariesClasspath = project.configurations.create(SPIGOT_LIBRARIES_CLASSPATH_CONFIGURATION_NAME).apply {
                isCanBeConsumed = false
                isCanBeResolved = true
                extendsFrom(compileOnlySpigot)
                description = "Resolvable view of ${COMPILE_ONLY_SPIGOT_CONFIGURATION_NAME} for generating plugin.yml libraries."

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
        val ctx = spec.createModuleRegistrationContext(project, project.provider { extension.toMap() })
        registerDescGenTask(project, ctx)
        val debugCtx = createPaperDebugRegistrationContext(project)
        PaperDebugTask.register(project, debugCtx)
    }
}
