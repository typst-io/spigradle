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

import io.typst.spigradle.Dependency
import io.typst.spigradle.VersionModifier

enum class NukkitDependencies(
    val group: String,
    val publicName: String,
    val version: String,
    val alias: String,
    val local: Boolean = false,
    val versionModifier: (String) -> String = { it },
) {
    NUKKIT(
        "cn.nukkit",
        "nukkit",
        "2.0.0-SNAPSHOT",
        "nukkit",
        false,
        VersionModifier.SNAPSHOT_APPENDER
    ),

    NUKKIT_X(
        NUKKIT.group,
        NUKKIT.publicName,
        NUKKIT.version,
        "nukkitX",
        NUKKIT.local,
        NUKKIT.versionModifier
    ),
    ;

    fun toDependency(): Dependency {
        return Dependency(group, publicName, version, local, versionModifier)
    }

    fun format(version: String? = null): String {
        return toDependency().format(version)
    }
}
