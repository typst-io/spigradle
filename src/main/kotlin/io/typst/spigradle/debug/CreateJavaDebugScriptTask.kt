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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

open class CreateJavaDebugScriptTask : DefaultTask() {
    init {
        group = "spigradle debug"
        description = "Writes script files to run the server jar for Windows/Unix"
    }

    @get:OutputDirectory
    val dir: DirectoryProperty = project.objects.directoryProperty()

    @get:Input
    val javaPath: Property<String> = project.objects.property(String::class.java)

    @get:Input
    val jvmArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

    @get:Input
    val programArgs: ListProperty<String> = project.objects.listProperty(String::class.java)

    @get:Input
    val jarFile: Property<String> = project.objects.property(String::class.java)

    @TaskAction
    fun create() {
        val dir = dir.get().asFile
        val batFile = dir.resolve("starter.bat")
        val javaFilePath = javaPath.get()
        val jvmArgsStr = jvmArgs.get().joinToString(" ")
        val jarFilePath = jarFile.get()
        val programArgsStr = programArgs.get().joinToString(" ")
        batFile.writeText(
            """
                            @echo off
                            "$javaFilePath" $jvmArgsStr -jar "$jarFilePath" $programArgsStr
                            pause:
                        """.trimIndent()
        )
        val shFile = dir.resolve("starter")
        try {
            Files.setPosixFilePermissions(
                shFile.toPath(),
                setOf(
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.OTHERS_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                )
            )
        } catch (th: Throwable) {
            // ignore
        }
        shFile.writeText(
            """
                            #!/usr/bin/env bash
                            "$javaFilePath" $jvmArgsStr -jar "$jarFilePath" $programArgsStr
                        """.trimIndent()
        )
    }
}