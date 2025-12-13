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

import io.typst.spigradle.common.NukkitRepositories
import io.typst.spigradle.common.Repositories
import io.typst.spigradle.common.Repositories.JITPACK
import io.typst.spigradle.common.Repositories.SONATYPE
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import javax.inject.Inject
import org.gradle.kotlin.dsl.maven

abstract class NukkitRepositoryExtension @Inject constructor(val project: Project) {
    /**
     * The repo shortcut for [Repositories.SONATYPE], related with Bungeecord.
     */
    fun sonatype(): MavenArtifactRepository = project.repositories.maven(Repositories.SONATYPE.address)

    /**
     * The repo shortcut for [Repositories.JITPACK], related with Vault.
     */
    fun jitpack(): MavenArtifactRepository = project.repositories.maven(Repositories.JITPACK.address)

    /**
     * The repo shortcut for openCollabRelease, related with NukkitX releases.
     */
    fun openCollabRelease(): MavenArtifactRepository = project.repositories.maven(NukkitRepositories.OPEN_COLLAB_RELEASE.address)

    /**
     * The repo shortcut for openCollabSnapshot, related with NukkitX snapshots.
     */
    fun openCollabSnapshot(): MavenArtifactRepository = project.repositories.maven(NukkitRepositories.OPEN_COLLAB_SNAPSHOT.address)

    /**
     * The repo shortcut for nukkitX same as openCollabSnapshot, related with NukkitX.
     */
    fun nukkitX(): MavenArtifactRepository = openCollabSnapshot()
}