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

import io.typst.spigradle.Repositories
import io.typst.spigradle.sonatype
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven

/**
 * The repo shortcut for Sonatype same as sonatype(), related with Bungeecord.
 */
fun RepositoryHandler.bungeecord(configure: MavenArtifactRepository.() -> Unit = {}) = sonatype(configure)
/**
 * The repo shortcut for Sonatype same as sonatype(), related with Bungeecord.
 */
fun RepositoryHandler.sonatype(configure: MavenArtifactRepository.() -> Unit = {}) = sonatype(configure)
/**
 * The repo shortcut for Sonatype same as sonatype(), related with Bungeecord.
 */
fun RepositoryHandler.minecraftLibraries(configure: MavenArtifactRepository.() -> Unit = {}) = maven(BungeeRepositories.MINECRAFT_LIBRARIES.address, configure)

/**
 * The dependency shortcut for Bungeecord, requires repository sonatype() or bungeecord().
 *
 * @param version Defaults to [BungeeDependencies.BUNGEE_CORD].version
 */
fun DependencyHandler.bungeecord(version: String? = null) = BungeeDependencies.BUNGEE_CORD.format(version)
/**
 * The dependency shortcut for Brigadier, requires repository minecraftLibraries().
 *
 * @param version Defaults to [BungeeDependencies.BUNGEE_CORD].version
 */
fun DependencyHandler.brigadier(version: String? = null) = BungeeDependencies.BRIGADIER.format(version)