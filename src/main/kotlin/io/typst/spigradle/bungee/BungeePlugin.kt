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

package io.typst.spigradle.bungee

import groovy.lang.Closure
import io.typst.spigradle.PluginConvention
import io.typst.spigradle.applySpigradlePlugin
import io.typst.spigradle.groovyExtension
import io.typst.spigradle.registerDescGenTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName

/**
 * The Bungeecord plugin that adds:
 * - [io.typst.spigradle.YamlGenerate] task for the 'bungee.yml' generation.
 * - [io.typst.spigradle.SubclassDetection] task for the main-class detection.
 * - Debug tasks for test your plugin.
 */
class BungeePlugin : Plugin<Project> {
    companion object {
        val BUNGEE_TYPE = PluginConvention(
            serverName = "bungee",
            descFile = "bungee.yml",
            mainSuperClass = "net/md_5/bungee/api/plugin/Plugin"
        )
    }

    val Project.bungee get() = extensions.getByName<BungeeExtension>("bungee")

    override fun apply(project: Project) {
        with(project) {
            applySpigradlePlugin()
            setupGroovyExtension()
            registerDescGenTask(BUNGEE_TYPE, BungeeExtension::class.java) { desc ->
                desc.encodeToMap()
            }
        }
    }

    private fun Project.setupGroovyExtension() {
        val depExt = dependencies.groovyExtension
        val repExp = repositories.groovyExtension
        for (dep in BungeeDependencies.values()) {
            depExt.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(vararg arguments: String) =
                    dep.format(arguments.getOrNull(0))
            })
        }
        for (repo in BungeeRepositories.values()) {
            repExp.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven { setUrl(repo.address) }
            })
        }
    }
}
