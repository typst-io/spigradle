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
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.Remote
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.io.File

internal object DebugTask {
    internal fun register(project: Project, ctx: DebugRegistrationContext): TaskProvider<Exec> {
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
        project.tasks.register("clean${ctx.platformName.capitalized()}", Delete::class.java) {
            group = ctx.taskGroupName

            delete(ctx.getDebugDir(project))
        }

        project.tasks.register("cleanCache${ctx.platformName.capitalized()}", Delete::class.java) {
            group = ctx.taskGroupName

            delete(ctx.getDownloadBaseDir(project))
        }
        val ideaModel = project.extensions["idea"] as IdeaModel
        ideaModel.project.settings {
            runConfigurations {
                create("Debug${ctx.platformName.capitalized()}", Remote::class.java) {
                    transport = Remote.RemoteTransport.SHARED_MEM
                    sharedMemoryAddress = project.name
                }
            }
        }
        return project.tasks.register(ctx.runDebugTaskName, Exec::class.java) {
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
            if (os.startsWith("windows")) {
                val pwsh = if (File("C:/Program Files/PowerShell/7/pwsh.exe").exists()) "pwsh" else "powershell"
                val scriptPath = File(debugDirPath, "starter.bat").absolutePath
                val cmds = listOf(
                    pwsh, "-NoProfile", "-Command",
                    "Start-Process", "cmd",
                    "-ArgumentList", "'/k','${scriptPath}'",
                    "-WorkingDirectory", debugDirPath,
                    "-WindowStyle", "Normal"
                )
                commandLine(cmds)
            } else if (os.contains("linux")) {
                val run = "cd '${debugDirPath.replace("'", "'\\''")}'; ./starter.sh; exec bash"
                val cli = when {
                    File("/usr/bin/gnome-terminal").exists() -> listOf("gnome-terminal", "--", "bash", "-lc", run)
                    File("/usr/bin/konsole").exists() -> listOf("konsole", "-e", "bash", "-lc", run)
                    File("/usr/bin/xfce4-terminal").exists() -> listOf("xfce4-terminal", "-e", "bash -lc \"$run\"")
                    else -> listOf("xterm", "-e", "bash", "-lc", run)
                }
                commandLine(cli)
            } else if (os.contains("mac")) {
                val cmds = listOf(
                    "osascript", "-e",
                    """
                    tell application "Terminal"
                      activate
                      do script "cd ${debugDirPath.replace(" ", "\\ ")}; ./starter.sh"
                    end tell
                    """.trimIndent()
                )
                commandLine(cmds)
            } else {
                commandLine("bash", "-lc", "cd '${debugDirPath.replace("'", "'\\''")}' && ./starter.sh")
            }
            isIgnoreExitValue = true

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
                        writeText("\"${javaExe.get()}\" -agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address=${project.name} -jar ${jarPath.get()} nogui\npause:")
                    }
                }
                debugDir.resolve("starter.ps1").apply {
                    if (!isFile) {
                        writeText("start powershell ./starter.bat")
                    }
                }
                debugDir.resolve("starter.sh").apply {
                    if (!isFile) {
                        writeText("\"${javaExe.get()}\" -agentlib:jdwp=transport=dt_shmem,server=y,suspend=n,address=${project.name} -jar ${jarPath.get()} nogui")
                    }
                }
            }
        }
    }
}
