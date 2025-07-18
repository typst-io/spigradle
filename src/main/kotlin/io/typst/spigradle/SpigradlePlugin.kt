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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.maven

fun Project.applySpigradlePlugin() = pluginManager.apply(SpigradlePlugin::class)

fun Project.getPluginMainPathFile(type: String) =
    layout.buildDirectory.file("spigradle/${type}_main").get().asFile

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
}
