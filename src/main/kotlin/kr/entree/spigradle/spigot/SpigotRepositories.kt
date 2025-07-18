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

package kr.entree.spigradle.spigot

enum class SpigotRepositories(val address: String, val alias: String) {
    // @SerialName("purpurmc"), purpur
    PURPUR_MC("https://repo.purpurmc.org/snapshots", "purpurmc"),

    // @SerialName("spigotmc"), spigot
    SPIGOT_MC("https://hub.spigotmc.org/nexus/content/repositories/snapshots/", "spigotmc"),

    // @SerialName("papermc"), paper
    PAPER_MC("https://repo.papermc.io/repository/maven-public/", "papermc"),
    PROTOCOL_LIB("https://repo.dmulloy2.net/nexus/repository/public/", "protocolLib"),

    // @SerialName("enginehub")
    ENGINE_HUB("https://maven.enginehub.org/repo/", "enginehub"),

    // @SerialName("codemc"), bstats
    CODE_MC("https://repo.codemc.org/repository/maven-public/", "codemc"),

    // essentialsX
    ENDER_ZONE("https://ci.ender.zone/plugin/repository/everything/", "enderzone"),

    // banManager
    FROSTCAST("https://ci.frostcast.net/plugin/repository/everything", "frostcast")
}