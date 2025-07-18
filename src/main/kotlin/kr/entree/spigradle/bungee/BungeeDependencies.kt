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

package kr.entree.spigradle.bungee

import kr.entree.spigradle.Dependency
import kr.entree.spigradle.VersionModifier

object BungeeDependencies {
    //    @SerialName("bungeecord")
    val BUNGEE_CORD = Dependency(
        "net.md-5",
        "bungeecord-api",
        "1.15-SNAPSHOT",
        false,
        VersionModifier.SNAPSHOT_APPENDER
    )
}
