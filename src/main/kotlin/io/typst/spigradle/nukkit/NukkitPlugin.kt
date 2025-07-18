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

package io.typst.spigradle.nukkit

import io.typst.spigradle.PluginConvention
import io.typst.spigradle.applySpigradlePlugin
import io.typst.spigradle.groovyExtension
import io.typst.spigradle.registerDescGenTask
import io.typst.spigradle.spigot.Load
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.maven

/**
 * The Nukkit plugin that adds:
 * - [io.typst.spigradle.YamlGenerate] task for the 'plugin.yml' generation.
 * - [io.typst.spigradle.SubclassDetection] task for the main-class detection.
 * - Debug tasks for test your plugin.
 */
class NukkitPlugin : Plugin<Project> {
    companion object {
        val NUKKIT_TYPE = PluginConvention(
            serverName = "nukkit",
            descFile = "plugin.yml",
            mainSuperClass = "cn/nukkit/plugin/PluginBase"
        )
    }

    val Project.nukkit get() = extensions.getByName<NukkitExtension>(NUKKIT_TYPE.descExtension)

    override fun apply(project: Project) {
        with(project) {
            applySpigradlePlugin()
            setupDefaultRepositories()
            registerDescGenTask(NUKKIT_TYPE, NukkitExtension::class.java) { desc ->
                linkedMapOf(
                    "main" to desc.main.orNull,
                    "name" to desc.name.orNull,
                    "version" to desc.version.orNull,
                    "description" to desc.description.orNull,
                    "website" to desc.website,
                    "authors" to desc.authors,
                    "api" to desc.api,
                    "load" to desc.load?.name,
                    "prefix" to desc.prefix,
                    "depend" to desc.depends,
                    "softdepend" to desc.softDepends,
                    "loadbefore" to desc.loadBefore,
                    "commands" to desc.commands.map {
                        it.serialize()
                    },
                    "permissions" to desc.permissions.map {
                        it.serialize()
                    },
                )
            }
            setupGroovyExtensions()
        }
    }

    private fun Project.setupDefaultRepositories() {
        repositories.maven(NukkitRepositories.NUKKIT_X)
    }

    private fun Project.setupGroovyExtensions() {
        extensions.getByName(NUKKIT_TYPE.descExtension).groovyExtension.apply {
            set("POST_WORLD", Load.POST_WORLD)
            set("POSTWORLD", Load.POST_WORLD)
            set("STARTUP", Load.STARTUP)
        }
    }
}