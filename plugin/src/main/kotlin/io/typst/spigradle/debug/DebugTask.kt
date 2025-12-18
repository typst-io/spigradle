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

import io.typst.spigradle.Download
import io.typst.spigradle.asCamelCase
import io.typst.spigradle.hasJavaBasePlugin
import io.typst.spigradle.hasJavaPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.*
import java.io.File
import java.net.URI

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

    /**
     * Registers debug-related tasks and IntelliJ IDEA run configurations.
     *
     * **Important:** There is NO task named `debugSpigot`. The `debugSpigot { }` block in build files
     * is a configuration extension ([DebugExtension]). The actual debug task is named `debug${ProjectName}`.
     *
     * ## Tasks Created
     *
     * - **`download${PlatformName}`** (optional) - [io.typst.spigradle.Download] or [Copy]
     *   - Downloads or copies the platform server JAR
     *
     * - **`copyArtifactJar`** - [Copy]
     *   - Copies your plugin JAR to the debug server's plugins folder
     *
     * - **`cleanDebug${ProjectName}`** - [Delete]
     *   - Cleans the project-specific debug folder at `.gradle/spigradle-debug/${platform}`
     *
     * - **`cleanCache${PlatformName}`** - [Delete]
     *   - Cleans the global JAR cache at `$GRADLE_USER_HOME/spigradle-debug-jars/`
     *
     * - **`createJavaDebugScript`** - [CreateJavaDebugScriptTask]
     *   - Generates platform-specific starter scripts (starter.bat/starter)
     *
     *   **`prepare${ProjectName}`** - [Task]
     *   - A lifecycle task to prepare debugs
     *   - **Dependencies:** `download${PlatformName}`, `copyArtifactJar`, `createJavaDebugScript`
     *
     * - **`debug${ProjectName}`** - [Task]
     *   - Main debug task that orchestrates the debug workflow
     *   - **Dependencies:** `parepare${ProjectName}`
     *
     * ## IntelliJ IDEA Run Configurations
     *
     * Two run configurations are automatically created:
     *
     * ### 1. `Debug${ProjectName}` - Remote JVM Debug Configuration **Recommended**
     *
     * **Purpose:** Attach debugger to an already-running server (lightweight)
     *
     * **Recommended workflow:**
     * 1. Run `debug${ProjectName}` task in a terminal or IntelliJ's "Run Gradle Task" window
     * 2. Server starts in a new terminal window with remote debugging enabled (port 5005 by default)
     * 3. Click the "Debug" button on the `Debug${ProjectName}` configuration to attach the debugger
     *
     * **Type:** Remote JVM Debug (connects to existing process)
     *
     * **Advantages:**
     * - Keeps IntelliJ IDE lightweight (server runs in separate terminal)
     * - Clean separation between server process and IDE
     * - Server output visible in dedicated terminal
     *
     * **Configuration:**
     * - Transport: Socket
     * - Port: Value from `debugSpigot.jvmDebugPort` (default: 5005)
     *
     * ### 2. `Run${ProjectName}` - JarApplication Run Configuration
     *
     * **Purpose:** All-in-one server launch from IntelliJ (convenience over performance)
     *
     * **Usage:**
     * - Click the "Run" button to start the server normally
     * - Click the "Debug" button to start the server and attach debugger in one step
     *
     * **Type:** JarApplication (runs the server JAR directly from IntelliJ)
     *
     * **Note:** This makes IntelliJ heavier as it manages the server process directly.
     * Use this only when you want absolute convenience and don't mind the performance impact.
     *
     * **Configuration:**
     * - JAR Path: Downloaded server JAR
     * - Working Directory: Debug server directory
     * - JVM Args: Configured via `debugSpigot.jvmArgs`
     * - Program Args: Configured via `debugSpigot.programArgs`
     * - Before Run: Automatically executes download, copy, and script creation tasks
     *
     * @param project The Gradle project
     * @param ctx Debug registration context with platform-specific settings
     * @return TaskProvider for the main debug task
     */
    internal fun register(project: Project, ctx: DebugRegistrationContext): TaskProvider<Task> {
        val archiveFile = ctx.jarTask?.flatMap { it.archiveFile }
        val download = ctx.downloadTask ?: if (ctx.downloadURI.isNotEmpty()) {
            project.tasks.register(ctx.downloadTaskName, Download::class.java) {
                group = ctx.taskGroupName

                uri.set(URI.create(ctx.downloadURI))
                outputFile.set(ctx.getDownloadOutputFile(project))
            }
        } else null
        val copyArtifactJarTask = if (project.hasJavaPlugin) {
            project.tasks.register("copyArtifactJar", Copy::class.java) {
                group = ctx.taskGroupName

                if (ctx.jarTask != null) {
                    dependsOn(ctx.jarTask)
                }
                if (archiveFile != null) {
                    from(archiveFile)
                }
                into(ctx.getDebugArtifactDir(project))

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
            }
        } else null
        val jar = ctx.getDownloadOutputFile(project)
        project.tasks.register("cleanDebug${project.name.asCamelCase(true)}", Delete::class.java) {
            group = ctx.taskGroupName
            description = "Clean the debug folder of the project: \$PROJECT_HOME/spigradle-debug/\$platform"

            delete(ctx.getDebugDir(project))
        }
        project.tasks.register("cleanCache${ctx.platformName.asCamelCase(true)}", Delete::class.java) {
            group = ctx.taskGroupName
            description = "Clean the global cache of the project: \$GRADLE_USER_HOME/spigradle-debug-jars/\$platform"

            delete(ctx.getDownloadBaseDir(project))
        }
        val createJavaDebugScriptTask =
            if (project.hasJavaBasePlugin) {
                project.tasks.register("createJavaDebugScript", CreateJavaDebugScriptTask::class.java) {
                    group = ctx.taskGroupName

                    dir.set(ctx.getDebugDir(project))
                    javaPath.set(ctx.javaExecutable.map { it.asFile.absolutePath })
                    jvmArgs.set(ctx.jvmArgs)
                    programArgs.set(ctx.programArgs)
                    jarFile.set(jar.map { it.asFile.absolutePath })
                }
            } else null
        val debugTasks = mutableListOf<TaskProvider<out Task>>()
        if (download != null) {
            debugTasks += download
        }
        if (copyArtifactJarTask != null) {
            debugTasks += copyArtifactJarTask
        }
        if (createJavaDebugScriptTask != null) {
            debugTasks += createJavaDebugScriptTask
        }
        debugTasks += ctx.extraTasks
        val prepareTask = project.tasks.register("prepare${project.name.asCamelCase(true)}") {
            group = ctx.taskGroupName
            description = "A lifecycle task to prepare debugs"

            dependsOn(debugTasks)
        }
        project.rootProject.pluginManager.withPlugin("org.jetbrains.gradle.plugin.idea-ext") {
            val ideaModel = project.rootProject.extensions["idea"] as IdeaModel
            ideaModel.project.settings {
                runConfigurations {
                    register("Debug${project.name.asCamelCase(true)}", Remote::class.java) {
                        transport = Remote.RemoteTransport.SOCKET
                        port = ctx.jvmDebugPort.get()
                    }
                    val runConfName = "Run${project.name.asCamelCase(true)}"
                    register(runConfName, JarApplication::class.java) {
                        val paperJarPath = jar.orNull?.asFile?.absolutePath
                        if (paperJarPath != null) {
                            jarPath = paperJarPath
                        } else {
                            project.logger.log(
                                LogLevel.WARN,
                                "[Spigradle] IDEA run configuration `${runConfName}` is missing the required setting 'debugSpigot#version', so its `Path to JAR` field is not configured."
                            )
                        }
                        workingDirectory = ctx.getDebugDir(project).asFile.absolutePath
                        jvmArgs = ctx.jvmArgs.get().joinToString(" ")
                        programParameters = ctx.programArgs.get().joinToString(" ")
//                        jrePath = ctx.javaExecutable.get().asFile.absolutePath
                        beforeRun {
                            register(prepareTask.name, GradleTask::class.java) {
                                this.task = prepareTask.get()
                            }
                        }
                    }
                }
            }
        }
        return project.tasks.register(ctx.getRunDebugTaskName(project)) {
            group = ctx.taskGroupName

            dependsOn(prepareTask)

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
                        logger.log(
                            LogLevel.LIFECYCLE,
                            "No terminal emulator found. Server started in background. You could also use the IDEA run configuration `Run\$ProjectName`."
                        )
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
