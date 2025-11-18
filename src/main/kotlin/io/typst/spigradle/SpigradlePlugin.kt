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

package io.typst.spigradle

import groovy.lang.Closure
import io.typst.spigradle.bungee.BungeeDependencies
import io.typst.spigradle.spigot.SpigotDependencies
import io.typst.spigradle.spigot.SpigotRepositories
import io.typst.spigradle.spigot.mockBukkit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.maven

fun Project.applySpigradlePlugin() = pluginManager.apply(SpigradlePlugin::class)

fun Project.getPluginMainPathFile(type: String) =
    layout.buildDirectory.file("spigradle/${type}_main").get().asFile

/**
 * The base plugin applies:
 *
 * plugins:
 *   - java
 *
 * groovy extensions:
 *   - dependencies
 *   - repositories
 */
class SpigradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            pluginManager.apply("java")
            setupGroovyExtensions()
        }
    }

    private fun Project.setupGroovyExtensions() {
        setupRepositoryExtensions()
        setupDependencyExtensions()
    }

    private fun Project.setupRepositoryExtensions() {
        val depExt = dependencies.groovyExtension
        val repoExt = repositories.groovyExtension
        // repo
        for (repo in Repositories.values()) {
            repoExt.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven(repo.address)
            })
        }
        for (repo in SpigotRepositories.values()) {
            repoExt.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven(repo.address)
            })
        }
        // dep
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
    }

    private fun Project.setupDependencyExtensions() {
        val ext = dependencies.groovyExtension
        for (dep in Dependencies.values()) {
            ext.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(version: String?) = dep.format(version)
            })
        }
    }
}
