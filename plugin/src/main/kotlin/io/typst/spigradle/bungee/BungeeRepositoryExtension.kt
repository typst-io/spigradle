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

import io.typst.spigradle.common.BungeeRepositories
import io.typst.spigradle.common.Repositories
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven
import javax.inject.Inject

abstract class BungeeRepositoryExtension @Inject constructor(val project: Project) {
    /**
     * The repo shortcut for sonatype, related with BungeeCord dependencies.
     */
    fun sonatype(): MavenArtifactRepository = project.repositories.maven(BungeeRepositories.SONATYPE.address)

    /**
     * The repo shortcut for [Repositories.JITPACK], related with Vault.
     */
    fun jitpack(): MavenArtifactRepository = project.repositories.maven(Repositories.JITPACK.address)

    /**
     * The repo shortcut for bungeecord, related with BungeeCord API.
     */
    fun bungeecord(): MavenArtifactRepository = sonatype()

    /**
     * The repo shortcut for minecraftLibraries, related with Minecraft libraries.
     */
    fun minecraftLibraries(): MavenArtifactRepository =
        project.repositories.maven(BungeeRepositories.MINECRAFT_LIBRARIES.address)
}