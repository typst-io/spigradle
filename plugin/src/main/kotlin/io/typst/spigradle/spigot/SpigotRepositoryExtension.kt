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

package io.typst.spigradle.spigot

import io.typst.spigradle.Repositories
import io.typst.spigradle.spigot.SpigotRepositories
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven
import javax.inject.Inject

abstract class SpigotRepositoryExtension @Inject constructor(val project: Project) {
    /**
     * The repo shortcut for [Repositories.SONATYPE], related with Bungeecord.
     */
    fun sonatype(): MavenArtifactRepository = project.repositories.maven(Repositories.SONATYPE.address)

    /**
     * The repo shortcut for [Repositories.JITPACK], related with Vault.
     */
    fun jitpack(): MavenArtifactRepository = project.repositories.maven(Repositories.JITPACK.address)
    /**
     * The repo shortcut for spigotmc, related with Spigot, MockBukkit...
     */
    fun spigotmc(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.SPIGOT_MC.address)

    /**
     * The repo shortcut for papermc, related with Paper.
     */
    fun papermc(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.PAPER_MC.address)

    /**
     * The repo shortcut for protocolLib, related with ProtocolLib.
     */
    fun protocolLib(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.PROTOCOL_LIB.address)

    /**
     * The repo shortcut for Jitpack, related with Vault.
     */
    fun vault(): MavenArtifactRepository = jitpack()

    /**
     * The repo shortcut for enginehub, related with worldedit, worldguard, commandhelper...
     */
    fun enginehub(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.ENGINE_HUB.address)

    /**
     * The repo shortcut for codemc, related with bStats, bStatsLite.
     */
    fun codemc(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.CODE_MC.address)

    /**
     * The repo shortcut for BStats same as codemc, related with bStats and bStatsLite.
     */
    fun bStats(): MavenArtifactRepository = codemc()

    /**
     * The repo shortcut for enderZone, related with EssentialsX.
     */
    fun enderZone(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.ENDER_ZONE.address)

    /**
     * The repo shortcut for essentialsX same as enderZone, related with EssentialsX.
     */
    fun essentialsX(): MavenArtifactRepository = enderZone()

    /**
     * The repo shortcut for frostcast, related with BanManager.
     */
    fun frostcast(): MavenArtifactRepository = project.repositories.maven(SpigotRepositories.FROSTCAST.address)

    /**
     * The repo shortcut for banManager same as frostcast, related with BanManager.
     */
    fun banManager(): MavenArtifactRepository = frostcast()
}