/*
 * Copyright (c) 2020 Spigradle contributors.
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

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

internal fun Project.ensureMinecraftEULA(directory: File, eula: Boolean) {
    File(directory, "eula.txt").takeIf { eulaFile ->
        val accepted = runCatching {
            Properties().apply {
                eulaFile.reader().use { reader ->
                    load(reader)
                }
            }.getProperty("eula", "false")?.toBoolean()
        }.getOrNull()
        accepted != true && (eula || logger.run {
            quiet("""
                Are you agree the Mojang EULA? (https://account.mojang.com/documents/minecraft_eula)
                Your input (y)es or (n)o:
            """.trimIndent())
            readLine()?.matches(Regex("(?i)y(es)?$")) == true || throw GradleException("""
                Please set the 'eula' property in spigot {} block to true if you agree the Mojang EULA.
                https://account.mojang.com/documents/minecraft_eula
            """.trimIndent()) // If input is not "y(es)", throw up
        })
    }?.apply {
        parentFile.mkdirs()
        writeText("""
            # Accepted by Spigradle
            # ${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())}
            eula=true
        """.trimIndent())
    }
}

internal fun findRuntimeDependencyNotations(p: Project): List<String> {
    return listOf("runtimeClasspath", "runtime").flatMap {
        val cfg = p.configurations.findByName(it)
        if (cfg?.isCanBeResolved == true) {
            cfg.resolvedConfiguration.firstLevelModuleDependencies
        } else emptyList()
    }.map {
        "${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}"
    }
}
