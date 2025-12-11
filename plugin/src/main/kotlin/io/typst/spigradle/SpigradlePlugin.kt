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
import io.typst.spigradle.common.Dependencies
import io.typst.spigradle.common.Repositories
import io.typst.spigradle.common.SpigotDependencies
import io.typst.spigradle.common.SpigotRepositories
import io.typst.spigradle.spigot.mockBukkit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.maven

/**
 * The base plugin applies:
 *
 * groovy extensions:
 *   - dependencies
 *   - repositories
 */
class SpigradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
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
        for (repo in Repositories.entries) {
            repoExt.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven(repo.address)
            })
        }
        for (repo in SpigotRepositories.entries) {
            repoExt.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = repositories.maven(repo.address)
            })
        }
        // dep
        depExt.set("mockBukkit", object : Closure<Any>(this, this) {
            fun doCall(vararg arguments: String) =
                dependencies.mockBukkit(arguments.getOrNull(0), arguments.getOrNull(1))
        })
        for (elem in SpigotDependencies.entries) {
            val dep = elem.dependency
            depExt.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(vararg arguments: String) =
                    dep.format(arguments.getOrNull(0))
            })
        }
        for (elem in BungeeDependencies.entries) {
            val dep = elem.dependency
            depExt.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(vararg arguments: String) =
                    dep.format(arguments.getOrNull(0))
            })
        }
    }

    private fun Project.setupDependencyExtensions() {
        val ext = dependencies.groovyExtension
        for (elem in Dependencies.entries) {
            val dep = elem.dependency
            ext.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(version: String?) = dep.format(version)
            })
        }
    }
}
