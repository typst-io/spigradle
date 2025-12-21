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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create

/**
 * The Nukkit base plugin that provides:
 *
 * Extensions (configuration blocks, NOT tasks):
 * - nukkit([NukkitExtension]): extension for the Nukkit environment
 * - repositories#nukkitRepos([NukkitRepositoryExtension]): extension for Nukkit repository DSL.
 */
class NukkitBasePlugin : Plugin<Project> {
    companion object {
        @JvmStatic
        val PLATFORM_NAME: String = "nukkit"
    }

    override fun apply(project: Project) {
        // register nukkit extension
        project.extensions.create(PLATFORM_NAME, NukkitExtension::class)
        // register repo ext
        (project.repositories as ExtensionAware).extensions.create(
            "nukkitRepos",
            NukkitRepositoryExtension::class,
            project
        )
    }
}