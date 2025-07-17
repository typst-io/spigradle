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

package kr.entree.spigradle

import groovy.lang.Closure
import kr.entree.spigradle.annotations.PluginType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.IdeaExtPlugin
import java.io.File

fun Project.applySpigradlePlugin() = pluginManager.apply(SpigradlePlugin::class)

val Gradle.spigotBuildToolDir get() = File(gradleUserHomeDir, SpigradlePlugin.SPIGOT_BUILD_TOOLS_DIR)

val Project.debugDir get() = File(projectDir, SpigradlePlugin.DEBUG_DIR)

// TODO: Remove in Spigradle 3.0
private val PluginType.internalName get() = if (this == PluginType.GENERAL) "plugin" else name.lowercase()

fun Project.getPluginMainPathFile(type: PluginType) =
    layout.buildDirectory.file("spigradle/${type.internalName}_main").get().asFile

class SpigradlePlugin : Plugin<Project> {
    companion object {
        const val DEBUG_DIR = "debug"
        const val SPIGOT_BUILD_TOOLS_DIR = "spigot-buildtools"
    }

    override fun apply(project: Project) {
        with(project) {
            setupPlugins()
            setupGroovyExtensions()
            markExcludeDirectories()
            setupTasks()
        }
    }

    private fun Project.setupPlugins() {
        rootProject.pluginManager.apply(IdeaPlugin::class)
        pluginManager.apply(IdeaExtPlugin::class)
    }

    private fun Project.setupGroovyExtensions() {
        setupRepositoryExtensions()
        setupDependencyExtensions()
    }

    private fun Project.setupRepositoryExtensions() {
        val ext = repositories.groovyExtension
        for (repo in Repositories.values()) {
            ext.set(repo.name.lowercase(), object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven(repo.address)
            })
        }
    }

    private fun Project.setupDependencyExtensions() {
        val ext = dependencies.groovyExtension
        Dependencies.ALL.forEach { (name, dependency) ->
            ext.set(name, object : Closure<Any>(this, this) {
                fun doCall(version: String?) = dependency.format(version)
            })
        }
    }

    private fun Project.markExcludeDirectories() {
        val idea: IdeaModel by extensions
        // Mark exclude directories
        idea.module {
            excludeDirs = setOf(debugDir) + excludeDirs
        }
    }

    private fun Project.setupTasks() {
        tasks.register("cleanDebug", Delete::class) {
            group = "spigradle"
            description = "Delete the debug directory."
            delete(debugDir)
        }
    }
}