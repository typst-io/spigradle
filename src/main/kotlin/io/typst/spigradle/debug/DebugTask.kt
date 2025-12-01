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
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.*
import java.io.File

private fun escb(xs: String): String =
    "\"${xs}\""

private fun escs(xs: String): String =
    xs.replace("'", "'\\''")

internal object DebugTask {
    internal fun findAvailableTerminal(workDir: String, scriptPath: String): List<String>? {
        if (isCommandAvailable("gnome-terminal")) {
            return listOf(
                "gnome-terminal", "--", "bash", "-c",
                "cd \"$workDir\" && \"$scriptPath\"; exec bash"
            )
        }
        if (isCommandAvailable("konsole")) {
            return listOf(
                "konsole", "--hold", "-e", "bash", "-c",
                "cd \"$workDir\" && \"$scriptPath\""
            )
        }
        if (isCommandAvailable("xfce4-terminal")) {
            return listOf(
                "xfce4-terminal", "--hold", "-e",
                "bash -c 'cd \"$workDir\" && \"$scriptPath\"'"
            )
        }
        if (isCommandAvailable("xterm")) {
            return listOf(
                "xterm", "-hold", "-e", "bash", "-c",
                "cd \"$workDir\" && \"$scriptPath\""
            )
        }
        if (isCommandAvailable("x-terminal-emulator")) {
            return listOf(
                "x-terminal-emulator", "-e", "bash", "-c",
                "cd \"$workDir\" && \"$scriptPath\"; exec bash"
            )
        }

        return null
    }

    internal fun isCommandAvailable(command: String): Boolean {
        return try {
            val process = ProcessBuilder("which", command)
                .redirectErrorStream(true)
                .start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    internal fun register(project: Project, ctx: DebugRegistrationContext): TaskProvider<Task> {
        val archiveFile = ctx.jarTask.flatMap { it.archiveFile }
        val downloadTask = ctx.downloadTask ?: if (ctx.downloadURI.isNotEmpty()) {
            project.tasks.register(ctx.downloadTaskName, Copy::class.java) {
                group = ctx.taskGroupName

                dependsOn(ctx.jarTask)
                from(archiveFile)
                into(ctx.getDebugArtifactDir(project))
            }
        } else null
        val copyArtifactJarTask = project.tasks.register("copyArtifactJar", Copy::class.java) {
            group = ctx.taskGroupName

            dependsOn(ctx.jarTask)
            from(archiveFile)
            into(ctx.getDebugArtifactDir(project))
        }
        val jar = ctx.getDownloadOutputFile(project)
        project.tasks.register("cleanDebug${project.name.caseKebabToPascal()}", Delete::class.java) {
            group = ctx.taskGroupName
            description = "Clean the debug folder of the project: \$PROJECT_HOME/spigradle-debug/\$platform"

            delete(ctx.getDebugDir(project))
        }
        project.tasks.register("cleanCache${ctx.platformName.capitalized()}", Delete::class.java) {
            group = ctx.taskGroupName
            description = "Clean the global cache of the project: \$GRADLE_USER_HOME/spigradle-debug-jars/\$platform"

            delete(ctx.getDownloadBaseDir(project))
        }
        val toolchains = project.extensions.getByType(JavaToolchainService::class.java)
        val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)
        val javaExe = project.providers.provider {
            toolchains.launcherFor {
                languageVersion.set(javaExt.toolchain.languageVersion.get())
            }.get().executablePath
        }
        val createJavaDebugScriptTask =
            project.tasks.register("createJavaDebugScript", CreateJavaDebugScriptTask::class.java) {
                group = ctx.taskGroupName

                dir.set(ctx.getDebugDir(project))
                javaPath.set(javaExe.map { it.asFile.absolutePath })
                jvmArgs.set(ctx.jvmArgs)
                programArgs.set(ctx.programArgs)
                jarFile.set(jar.map { it.asFile.absolutePath })
            }
        val debugTasks = mutableListOf<TaskProvider<out Task>>()
        if (downloadTask != null) {
            debugTasks += downloadTask
        }
        debugTasks += copyArtifactJarTask
        debugTasks += createJavaDebugScriptTask
        val ideaModel = project.rootProject.extensions["idea"] as IdeaModel
        ideaModel.project.settings {
            runConfigurations {
                register("Debug${project.name.caseKebabToPascal()}", Remote::class.java) {
                    transport = Remote.RemoteTransport.SOCKET
                    port = ctx.jvmDebugPort.get()
                }
                register("Run${project.name.caseKebabToPascal()}", JarApplication::class.java) {
                    jarPath = jar.get().asFile.absolutePath
                    workingDirectory = ctx.getDebugDir(project).asFile.absolutePath
                    jvmArgs = ctx.jvmArgs.get().joinToString(" ")
                    programParameters = ctx.programArgs.get().joinToString(" ")
                    beforeRun {
                        for (task in debugTasks) {
                            register(task.name, GradleTask::class.java) {
                                this.task = task.get()
                            }
                        }
                    }
                }
            }
        }
        return project.tasks.register(ctx.getRunDebugTaskName(project)) {
            group = ctx.taskGroupName

            if (downloadTask != null) {
                dependsOn(downloadTask)
            }
            dependsOn(copyArtifactJarTask, createJavaDebugScriptTask)

            val os = System.getProperty("os.name").lowercase()
            val debugDirPath = ctx.getDebugDir(project).asFile.absolutePath

            val cmds = if ("windows" in os) {
                val scriptPath = File(debugDirPath, "starter.bat").absolutePath
                listOf(
                    "cmd", "/c",
                    "start", "\"${project.name}\"",
                    "/D", debugDirPath,
                    scriptPath
                )
            } else if ("mac" in os) {
                val scriptPath = File(debugDirPath, "starter").absolutePath
                // single quote 사용, single quote 자체는 이스케이프
                val escapedDir = debugDirPath.replace("'", "'\\''")
                val escapedScript = scriptPath.replace("'", "'\\''")
                val appleScript = """
                    tell application "Terminal"
                        do script "cd '$escapedDir' && '$escapedScript'"
                        activate
                    end tell
                """.trimIndent()
                listOf("osascript", "-e", appleScript)
            } else {
                emptyList()
            }

            doFirst {
                if (ctx.eula?.get() == false) {
                    throw GradleException("Please set 'eula.set(true)' in the debug extension!")
                }
                val debugDir = ctx.getDebugDir(project).asFile
                if (ctx.eula?.get() == true) {
                    val eulaTxt = debugDir.resolve("eula.txt")
                    eulaTxt.writeText("eula=true")
                }
            }

            doLast {
                var theCmds = cmds
                if (cmds.isEmpty()) {
                    val scriptPath = File(debugDirPath, "starter").absolutePath

                    val terminalCmd = findAvailableTerminal(debugDirPath, scriptPath)

                    if (terminalCmd != null) {
                        theCmds = terminalCmd
                    } else {
                        // nohup + setsid
                        theCmds = listOf(
                            "/bin/sh", "-c",
                            "cd \"$debugDirPath\" && nohup setsid \"$scriptPath\" > /dev/null 2>&1 &"
                        )
                        logger.log(LogLevel.LIFECYCLE, "No terminal emulator found. Server started in background. You could also use the IDEA run configuration `Run\$ProjectName`.")
                    }
                }

                val process = ProcessBuilder(theCmds)
                    .directory(File(debugDirPath))
                    .redirectErrorStream(true)
                    .start()

                val exited = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)
                if (exited) {
                    val exitCode = process.exitValue()
                    if (exitCode != 0) {
                        throw GradleException("Process exit code: $exitCode")
                    }
                } else {
                    logger.log(LogLevel.LIFECYCLE, "Server launched successfully.")
                }
            }
        }
    }
}
