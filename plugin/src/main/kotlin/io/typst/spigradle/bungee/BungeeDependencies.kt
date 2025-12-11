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

import io.typst.spigradle.common.Dependency

internal enum class BungeeDependencies(
    val dependency: Dependency,
) {
    //    @SerialName("bungeecord")
    BUNGEE_CORD(
        Dependency(
        "net.md-5",
        "bungeecord-api",
        "1.21-R0.4",
        "bungeecord"
        )
    ),

    BRIGADIER(
        Dependency(
        "com.mojang",
        "brigadier",
        "1.0.18",
        "brigadier"
        )
    )
    ;

    fun format(version: String?): String {
        return dependency.format(version)
    }
}
