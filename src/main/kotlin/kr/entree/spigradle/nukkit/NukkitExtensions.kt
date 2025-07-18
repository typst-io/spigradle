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

package kr.entree.spigradle.nukkit

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven

/**
 * The repo shortcut for NukkitX, related with Nukkit.
 */
fun RepositoryHandler.nukkitX(configure: MavenArtifactRepository.() -> Unit = {}) = maven(NukkitRepositories.NUKKIT_X, configure)

/**
 * The dependency shortcut for Nukkit, NukkitX, requires repository NukkitX.
 *
 * @param version Defaults to [NukkitDependencies.NUKKIT].version
 */
fun DependencyHandler.nukkit(version: String? = null) = NukkitDependencies.NUKKIT.format(version)