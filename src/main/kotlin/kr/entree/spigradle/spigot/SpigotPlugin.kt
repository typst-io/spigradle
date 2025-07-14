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

package kr.entree.spigradle.spigot

import groovy.lang.Closure
import kr.entree.spigradle.annotations.PluginType
import kr.entree.spigradle.applyToConfigure
import kr.entree.spigradle.groovyExtension
import kr.entree.spigradle.runConfigurations
import kr.entree.spigradle.settings
import kr.entree.spigradle.kotlin.mockBukkit
import kr.entree.spigradle.PluginConvention
import kr.entree.spigradle.applySpigradlePlugin
import kr.entree.spigradle.createRunConfigurations
import kr.entree.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.JarApplication

/**
 * The Spigot plugin that adds:
 * - [kr.entree.spigradle.YamlGenerate] task for the 'plugin.yml' generation.
 * - [kr.entree.spigradle.SubclassDetection] task for the main-class detection.
 * - Debug tasks for test your plugin.
 */
class SpigotPlugin : Plugin<Project> {
    companion object {
        val SPIGOT_TYPE = PluginConvention(
            serverName = "spigot",
            descFile = "plugin.yml",
            mainSuperClass = "org/bukkit/plugin/java/JavaPlugin",
            mainType = PluginType.SPIGOT
        )
    }

    private val Project.spigot get() = extensions.getByName<SpigotExtension>(SPIGOT_TYPE.descExtension)

    override fun apply(project: Project) {
        with(project) {
            applySpigradlePlugin()
            setupDefaultDependencies()
            // TODO: auto libraries
            registerDescGenTask(SPIGOT_TYPE, SpigotExtension::class.java) { desc ->
                mapOf(
                    "main" to desc.main,
                    "name" to desc.name,
                    "version" to desc.version,
                    "description" to desc.description,
                    "website" to desc.website,
                    "authors" to desc.authors,
                    "api-version" to desc.apiVersion,
                    "load" to desc.load?.name,
                    "prefix" to desc.prefix,
                    "depend" to desc.depends,
                    "softdepend" to desc.softDepends,
                    "loadbefore" to desc.loadBefore,
                    "libraries" to desc.libraries,
                    "commands" to desc.commands.toList().map {
                        TODO(it.toString())
                    },
                    "permissions" to desc.permissions.toList().map {
                        TODO(it.toString())
                    },
                )
            }
            setupGroovyExtensions()
        }
    }

    private fun Project.setupDefaultDependencies() {
        val ext = dependencies.groovyExtension
        ext.set("mockBukkit", object : Closure<Any>(this, this) {
            fun doCall(vararg arguments: String) =
                dependencies.mockBukkit(arguments.getOrNull(0), arguments.getOrNull(1))
        }) // Can be replaced by reflection to SpigotExtensionsKt
    }

    private fun Project.setupGroovyExtensions() {
        spigot.groovyExtension.apply {
            set("POST_WORLD", Load.POST_WORLD)
            set("POSTWORLD", Load.POST_WORLD)
            set("STARTUP", Load.STARTUP)
        }
    }
}
