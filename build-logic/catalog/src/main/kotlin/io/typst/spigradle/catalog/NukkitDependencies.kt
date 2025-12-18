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

package io.typst.spigradle.catalog

// NOTE: https://blog.gradle.org/best-practices-naming-version-catalog-entries#catalog-entry-naming-conventions
enum class NukkitDependencies(
    val dependency: Dependency,
) {
    NUKKIT(
        Dependency(
            "cn.nukkit",
            "nukkit",
            "1.0-SNAPSHOT",
            "nukkit",
            false,
            Dependency.SNAPSHOT_TAG
        )
    ),

    NUKKIT_X(
        NUKKIT.dependency.copy(
            alias = "nukkitX",
        )
    ),
    ;

    fun format(version: String? = null): String {
        return dependency.format(version)
    }
}
