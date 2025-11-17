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

package io.typst.spigradle.debug

import io.typst.spigradle.capitalized
import io.typst.spigradle.caseKebabToPascal
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.Remote
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

private fun escb(xs: String): String =
    "\"${xs}\""

private fun escs(xs: String): String =
    xs.replace("'", "'\\''")

internal object DebugTask {
    internal fun register(project: Project, ctx: DebugRegistrationContext): TaskProvider<Task> {
        // TODO: download, copyPlugin, writeFile(eula.txt), JavaExec
        val jarFile = ctx.jarTask.flatMap { it.archiveFile }
        val downloadTask = if (ctx.downloadURI.isNotEmpty()) {
            project.tasks.register(ctx.downloadTaskName, Copy::class.java) {
                group = ctx.taskGroupName

                dependsOn(ctx.jarTask)
                from(jarFile)
                into(ctx.getDebugArtifactDir(project))
            }
        } else null
        val copyArtifactJarTask = project.tasks.register("copyArtifactJar", Copy::class.java) {
            group = ctx.taskGroupName

            dependsOn(ctx.jarTask)
            from(jarFile)
            into(ctx.getDebugArtifactDir(project))
        }
        val jarPath = ctx.getDownloadOutputFile(project).map { it.asFile.absolutePath }
        project.tasks.register("cleanDebug${project.name.caseKebabToPascal()}", Delete::class.java) {
            group = ctx.taskGroupName
            description = "Clean the debug folder of ${project.name}: ${ctx.getDebugDir(project).absolutePath}"

            delete(ctx.getDebugDir(project))
        }
        project.tasks.register("cleanCache${ctx.platformName.capitalized()}", Delete::class.java) {
            group = ctx.taskGroupName
            description = "Clean the global cache of ${ctx.platformName}: ${ctx.getDownloadBaseDir(project)}"

            delete(ctx.getDownloadBaseDir(project))
        }
        val ideaModel = project.rootProject.extensions["idea"] as IdeaModel
        ideaModel.project.settings {
            runConfigurations {
                create("Debug${project.name.caseKebabToPascal()}", Remote::class.java) {
                    transport = Remote.RemoteTransport.SHARED_MEM
                    sharedMemoryAddress = project.name
                }
            }
        }
        return project.tasks.register(ctx.getRunDebugTaskName(project)) {
            group = ctx.taskGroupName

            if (downloadTask != null) {
                dependsOn(downloadTask)
            }
            dependsOn(copyArtifactJarTask)

            val toolchains = project.extensions.getByType(JavaToolchainService::class.java)
            val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)
            val javaExe = project.providers.provider {
                toolchains.launcherFor {
                    languageVersion.set(javaExt.toolchain.languageVersion.get())
                }.get().executablePath.asFile.absolutePath
            }
            val os = System.getProperty("os.name").lowercase()
            val debugDirPath = ctx.getDebugDir(project).absolutePath

            val jvmArgs = ctx.jvmArgs.orNull?.joinToString(" ") ?: ""
            val programArgs = ctx.programArgs.orNull?.joinToString(" ") ?: ""
            val cmds = if (os.startsWith("windows")) {
                val scriptPath = File(debugDirPath, "starter.bat").absolutePath
                val cmds = listOf(
                    "cmd", "/c",
                    "start", "\"${project.name}\"",
                    "/D", debugDirPath,
                    scriptPath,
                    escb(javaExe.get()),
                    escb(jvmArgs),
                    escb(jarPath.get()),
                    escb(programArgs),
                )
                cmds
            } else {

                val debugDirEscaped = escs(debugDirPath)
                val javaEsc = escs(javaExe.get())

                val script =
                    "cd '$debugDirEscaped' && ./starter.sh '$javaEsc' '${escs(jvmArgs)}' '${escs(jarPath.get())}' '${
                        escs(programArgs)
                    }'"
                listOf("sh", "-lc", script)
            }

            doFirst {
                if (ctx.eula?.get() == false) {
                    throw GradleException("Please set 'eula.set(true)' in the debug extension!")
                }
                val debugDir = ctx.getDebugDir(project)
                if (ctx.eula?.get() == true) {
                    val eulaTxt = debugDir.resolve("eula.txt")
                    eulaTxt.writeText("eula=true")
                }
                debugDir.resolve("starter.bat").apply {
                    if (!isFile) {
                        writeText(
                            """
                            @echo off
                            REM %1 = javaExe, %2 = jvmArgs, %3 = jarPath, %4 = programArgs
                            "%~1" %~2 -jar "%~3" %4
                            pause:
                        """.trimIndent()
                        )
                    }
                }
                debugDir.resolve("starter.sh").apply {
                    if (!isFile) {
                        writeText(
                            """
                            #!/usr/bin/env bash
                            # $1 = javaExe, $2 = jvmArgs, $3 = jarPath, $4 = programArgs
                            "%~1" %~2 -jar "%~3" %4
                        """.trimIndent()
                        )
                        try {
                            Files.setPosixFilePermissions(
                                toPath(),
                                setOf(
                                    PosixFilePermission.OWNER_EXECUTE,
                                    PosixFilePermission.OWNER_READ,
                                    PosixFilePermission.GROUP_EXECUTE,
                                    PosixFilePermission.GROUP_READ,
                                    PosixFilePermission.OTHERS_EXECUTE,
                                    PosixFilePermission.OTHERS_READ,
                                )
                            )
                        } catch (th: Throwable) {
                            // ignore
                        }
                    }
                }
            }

            doLast {
                val process = ProcessBuilder(cmds).inheritIO().start()
                val processCode = process.waitFor()
                if (processCode != 0) {
                    throw GradleException("Process exit code: $processCode")
                }
            }
        }
    }
}
