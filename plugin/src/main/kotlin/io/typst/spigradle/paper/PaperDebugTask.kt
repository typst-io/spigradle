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

import io.typst.spigradle.debug.DebugExtension
import io.typst.spigradle.debug.DebugTask
import io.typst.spigradle.spigot.PaperDownloadTask
import io.typst.spigradle.spigot.PluginDependencyPrepareTask
import io.typst.spigradle.spigot.SpigotPlugin.Companion.getMinecraftMinimumJavaVersion
import org.gradle.api.GradleException
import org.gradle.api.Project

internal object PaperDebugTask {
    internal fun register(project: Project, paperCtx: PaperDebugRegistrationContext) {
        val ctx = paperCtx.ctx
        val paperDebugExt = project.extensions.getByType(DebugExtension::class.java).apply {
            jvmArgs.convention(jvmDebugPort.map { port ->
                listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${port}")
            })
            programArgs.convention(listOf("nogui"))
        }
        val downloadPaper = project.tasks.register("downloadPaper", PaperDownloadTask::class.java) {
            group = ctx.taskGroupName

            version.set(paperDebugExt.version)
            outputFile.set(ctx.getDownloadOutputFile(project))
        }
        val preparePluginDependencies =
            project.tasks.register("preparePluginDependencies", PluginDependencyPrepareTask::class.java) {
                group = ctx.taskGroupName

                pluginNames.set(paperDebugExt.downloadSoftDepend.map { downloadSoftDepend ->
                    if (downloadSoftDepend) {
                        (paperCtx.depend.orNull ?: emptySet()) + (paperCtx.softDepend.orNull ?: emptySet())
                    } else paperCtx.depend.orNull ?: emptySet()
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