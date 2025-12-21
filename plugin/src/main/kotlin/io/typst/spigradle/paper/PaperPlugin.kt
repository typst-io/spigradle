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

package io.typst.spigradle.paper

import io.typst.spigradle.PlatformPluginSpec
import io.typst.spigradle.PluginDescriptionProperty
import io.typst.spigradle.debug.DebugExtension
import io.typst.spigradle.debug.DebugRegistrationContext
import io.typst.spigradle.paper.PaperBasePlugin.Companion.PLATFORM_NAME
import io.typst.spigradle.propertyPlugin
import io.typst.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Jar

// NOTE: won't release yet until the paper-plugin.yml becomes stable.
class PaperPlugin : Plugin<Project> {
    companion object {
        internal val spec: PlatformPluginSpec = PlatformPluginSpec(
            PLATFORM_NAME,
            "paper-plugin.yml",
            listOf(
                PluginDescriptionProperty("main", "org/bukkit/plugin/java/JavaPlugin", mandatory = true),
                PluginDescriptionProperty("bootstrapper", "io/papermc/paper/plugin/bootstrap/PluginBootstrap"),
                PluginDescriptionProperty("loader", "io/papermc/paper/plugin/loader/PluginLoader"),
                PluginDescriptionProperty("api-version", mandatory = true),
            )
        )

        @JvmStatic
        val GENERATE_PLUGIN_DESCRIPTION_TASK_NAME: String = spec.generateDescriptionTaskName

        @JvmStatic
        val DETECT_ENTRYPOINTS_TASK_NAME: String = spec.detectEntrypointsTaskName

        internal fun createPaperDebugRegistrationContext(
            project: Project,
            depend: Provider<Set<String>>,
            softDepend: Provider<Set<String>>,
        ): PaperDebugRegistrationContext {
            val debugExtension = project.extensions.getByType(DebugExtension::class.java)
            val javaPlugin = project.propertyPlugin("java")
            val jarFile = javaPlugin.flatMap {
                val jarFile = project.tasks.named("jar", Jar::class.java).flatMap { it.archiveFile }
                debugExtension.jarFile.convention(jarFile)
            }
            val subCtx = DebugRegistrationContext(
                PLATFORM_NAME,
                debugExtension.version,
                "",
                "plugins",
                jarFile,
                debugExtension.jvmArgs,
                debugExtension.programArgs,
                debugExtension.jvmDebugPort,
                debugExtension.javaHome.map { it.file("bin/java") },
                false,
                debugExtension.eula
            )
            return PaperDebugRegistrationContext(
                subCtx,
                depend,
                softDepend
            )
        }

        internal fun createPaperDebugRegistrationContext(
            project: Project,
        ): PaperDebugRegistrationContext {
            val extension = project.extensions.getByType(PaperExtension::class.java)
            val depend = project.provider {
                extension.serverDependencies.flatMap {
                    if (it.load.get() == "BEFORE" && it.required.get()) {
                        listOf(it.name)
                    } else emptyList()
                }.toSet()
            }
            val softDepend = project.provider {
                extension.serverDependencies.flatMap {
                    if (it.load.get() != "BEFORE" || it.required.get()) {
                        listOf(it.name)
                    } else emptyList()
                }.toSet()
            }
            return createPaperDebugRegistrationContext(project, depend, softDepend)
        }
    }

    override fun apply(project: Project) {
        project.pluginManager.apply(JavaBasePlugin::class.java)
        project.pluginManager.apply(PaperBasePlugin::class.java)

        val ext = project.extensions.getByType(PaperExtension::class.java)

        val ctx = spec.createModuleRegistrationContext(
            project, project.provider {
                ext.toMap()
            }, mapOf(
                "bootstrapper" to ext.enableBootstrapper,
                "loader" to ext.enableLoader
            )
        )
        registerDescGenTask(project, ctx)
        val debugCtx = createPaperDebugRegistrationContext(project)
        PaperDebugTask.register(project, debugCtx)
    }
}