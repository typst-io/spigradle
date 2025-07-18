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

package io.typst.spigradle.spigot

import groovy.lang.Closure
import io.typst.spigradle.PluginConvention
import io.typst.spigradle.applySpigradlePlugin
import io.typst.spigradle.bungee.BungeeDependencies
import io.typst.spigradle.groovyExtension
import io.typst.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName

/**
 * The Spigot plugin that adds:
 * - [io.typst.spigradle.YamlGenerate] task for the 'plugin.yml' generation.
 * - [io.typst.spigradle.SubclassDetection] task for the main-class detection.
 * - Debug tasks for test your plugin.
 */
class SpigotPlugin : Plugin<Project> {
    companion object {
        val SPIGOT_TYPE = PluginConvention(
            serverName = "spigot",
            descFile = "plugin.yml",
            mainSuperClass = "org/bukkit/plugin/java/JavaPlugin"
        )
    }

    private val Project.spigot get() = extensions.getByName<SpigotExtension>(SPIGOT_TYPE.descExtension)

    override fun apply(project: Project) {
        with(project) {
            applySpigradlePlugin()
            // TODO: auto libraries
            registerDescGenTask(SPIGOT_TYPE, SpigotExtension::class.java) { desc ->
                linkedMapOf(
                    "main" to desc.main.orNull,
                    "name" to desc.name.orNull,
                    "version" to desc.version.orNull,
                    "description" to desc.description.orNull,
                    "website" to desc.website,
                    "authors" to desc.authors.ifEmpty { null },
                    "api-version" to desc.apiVersion,
                    "load" to desc.load?.name,
                    "prefix" to desc.prefix,
                    "depend" to desc.depends.ifEmpty { null },
                    "softdepend" to desc.softDepends.ifEmpty { null },
                    "loadbefore" to desc.loadBefore.ifEmpty { null },
                    "libraries" to desc.libraries.ifEmpty { null },
                    "commands" to desc.commands.toList().associate {
                        it.name to it.serialize()
                    }.ifEmpty { null },
                    "permissions" to desc.permissions.toList().associate {
                        it.name to it.serialize()
                    }.ifEmpty { null },
                ).filterValues {
                    it != null
                }
            }
            setupGroovyExtensions()
        }
    }

    private fun Project.setupGroovyExtensions() {
        val depExt = dependencies.groovyExtension
        val repExp = repositories.groovyExtension
        // dependencies
        depExt.set("mockBukkit", object : Closure<Any>(this, this) {
            fun doCall(vararg arguments: String) =
                dependencies.mockBukkit(arguments.getOrNull(0), arguments.getOrNull(1))
        })
        for (dep in SpigotDependencies.values()) {
            depExt.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(vararg arguments: String) =
                    dep.format(arguments.getOrNull(0))
            })
        }
        for (dep in BungeeDependencies.values()) {
            depExt.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(vararg arguments: String) =
                    dep.format(arguments.getOrNull(0))
            })
        }
        // repositories
        for (repo in SpigotRepositories.values()) {
            repExp.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven { setUrl(repo.address) }
            })
        }
        // literal
        depExt.set("POST_WORLD", Load.POST_WORLD)
        depExt.set("POSTWORLD", Load.POST_WORLD)
        depExt.set("STARTUP", Load.STARTUP)
    }
}
