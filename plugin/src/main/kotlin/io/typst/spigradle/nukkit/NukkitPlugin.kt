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

import groovy.lang.Closure
import io.typst.spigradle.*
import io.typst.spigradle.common.NukkitDependencies
import io.typst.spigradle.common.NukkitRepositories
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create

/**
 * The Nukkit plugin that adds:
 * - [io.typst.spigradle.YamlGenerate] task for the 'plugin.yml' generation.
 * - [io.typst.spigradle.SubclassDetection] task for the main-class detection.
 * - Debug tasks for test your plugin.
 */
class NukkitPlugin : Plugin<Project> {
    companion object {
        val platformName = "nukkit"
        val genDescTask: String = "generate${platformName.asCamelCase(true)}Description"
        val mainDetectTask: String = "detect${platformName.asCamelCase(true)}Main"

        fun createModuleRegistrationContext(
            project: Project,
            extension: NukkitExtension,
        ): ModuleRegistrationContext<NukkitExtension> {
            return ModuleRegistrationContext(
                platformName,
                "plugin.yml",
                extension,
                project.getMainDetectOutputFile(platformName),
                genDescTask,
                mainDetectTask,
                "cn/nukkit/plugin/PluginBase"
            )
        }
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create(platformName, NukkitExtension::class)
        val ctx = createModuleRegistrationContext(project, extension)
        registerDescGenTask(project, ctx) { desc ->
            desc.toMap()
        }
        setupGroovyExtensions(project)

        // register repo
        (project.repositories as ExtensionAware).extensions.create(
            "nukkitRepos",
            NukkitRepositoryExtension::class,
            project
        )
    }

    private fun setupGroovyExtensions(project: Project) {
        val depExt = project.dependencies.groovyExtension
        val repExp = project.repositories.groovyExtension
        for (element in NukkitDependencies.entries) {
            val dep = element.dependency
            depExt.set(dep.alias, object : Closure<Any>(this, this) {
                fun doCall(vararg arguments: String) =
                    dep.format(arguments.getOrNull(0))
            })
        }
        for (repo in NukkitRepositories.entries) {
            repExp.set(repo.alias, object : Closure<Any>(this, this) {
                fun doCall() = project.repositories.maven { setUrl(repo.address) }
            })
        }
    }
}