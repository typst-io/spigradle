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

package kr.entree.spigradle.module.common

import de.undercouch.gradle.tasks.download.DownloadTaskPlugin
import groovy.lang.Closure
import kr.entree.spigradle.annotations.PluginType
import kr.entree.spigradle.data.Dependencies
import kr.entree.spigradle.data.Repositories
import kr.entree.spigradle.data.Repositories.SONATYPE
import kr.entree.spigradle.internal.groovyExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.IdeaExtPlugin
import java.io.File

fun Project.applySpigradlePlugin() = pluginManager.apply(SpigradlePlugin::class)

val Gradle.spigotBuildToolDir get() = File(gradleUserHomeDir, SpigradlePlugin.SPIGOT_BUILD_TOOLS_DIR)

val Project.debugDir get() = File(projectDir, SpigradlePlugin.DEBUG_DIR)

// TODO: Remove in Spigradle 3.0
private val PluginType.internalName get() = if (this == PluginType.GENERAL) "plugin" else name.toLowerCase()

fun Project.getPluginMainPathFile(type: PluginType) = File(buildDir, "spigradle/${type.internalName}_main")

class SpigradlePlugin : Plugin<Project> {
    companion object {
        const val DEBUG_DIR = "debug"
        const val SPIGOT_BUILD_TOOLS_DIR = "spigot-buildtools"
    }

    override fun apply(project: Project) {
        project.logger.log(LogLevel.WARN,
            """NOTE: This plugin deprecated and changed the plugin id to 'io.typst.spigradle.spigot', new repository is here:
https://github.com/typst-io/spigradle"""
        )
        with(project) {
            setupPlugins()
            setupDefaultDependencies()
            setupDefaultRepositories()
            setupGroovyExtensions()
            setupAnnotationProcessorOptions()
            markExcludeDirectories()
            setupTasks()
        }
    }

    @Suppress("UnstableApiUsage")
    private fun Project.setupPlugins() {
        pluginManager.apply(JavaPlugin::class)
        rootProject.pluginManager.apply(IdeaPlugin::class)
        pluginManager.apply(IdeaExtPlugin::class)
        pluginManager.apply(DownloadTaskPlugin::class)
        if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            plugins.apply("org.jetbrains.kotlin.kapt")
            // Give annotation processor arguments for Kotlin
            val kapt = extensions.getByName("kapt")
            val kaptArgs = PluginType.values()
                .map { type -> type.pathKey to getPluginMainPathFile(type) }
            kapt.withGroovyBuilder {
                "arguments" {
                    kaptArgs.forEach { (k, v) ->
                        "arg"(k, v)
                    }
                }
            }
            afterEvaluate {
                // Task ordering
                val kaptKotlin: Task by tasks
                tasks.withType(YamlGenerate::class) {
                    kaptKotlin.finalizedBy(this)
                }
            }
        }
    }

    private fun Project.setupDefaultDependencies() {
        dependencies.apply {
            val notation = Dependencies.SPIGRADLE_ANNOTATIONS.format()
            add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, notation)
            if (project.configurations.findByName("kapt") != null) {
                add("kapt", notation)
            } else {
                add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, notation)
            }
        }
    }

    private fun Project.setupDefaultRepositories() {
        repositories.gradlePluginPortal() // For avoid APT errors
        repositories.mavenCentral()
        repositories.maven(SONATYPE)
    }

    private fun Project.setupGroovyExtensions() {
        setupRepositoryExtensions()
        setupDependencyExtensions()
    }

    private fun Project.setupRepositoryExtensions() {
        val ext = repositories.groovyExtension
        Repositories.ALL.forEach { (name, url) ->
            ext.set(name, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven(url)
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

    private fun Project.setupAnnotationProcessorOptions() {
        val compileJava: JavaCompile by tasks
        val aptArgs = PluginType.values().map { type ->
            "-A${type.pathKey}=${getPluginMainPathFile(type)}"
        }
        compileJava.options.compilerArgs.addAll(aptArgs)
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