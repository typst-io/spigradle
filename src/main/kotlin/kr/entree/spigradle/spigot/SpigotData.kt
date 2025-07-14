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

import kr.entree.spigradle.Dependency
import kr.entree.spigradle.VersionModifier
import javax.inject.Inject

enum class Load {
    // @SerialName("POSTWORLD")
    POST_WORLD,
    STARTUP
}

// @JsonPropertyOrder("description", "usage", "permission", "permission-message")
open class Command @Inject constructor(@Transient val name: String) {
    var description: String? = null
    var usage: String? = null
    var permission: String? = null

    // @SerialName("permission-message")
    var permissionMessage: String? = null
    var aliases = emptyList<String>()

    fun aliases(vararg aliases: String) {
        this.aliases = aliases.toList()
    }

    fun serialize(): Map<String, Any?> {
        return mapOf(
            "description" to description,
            "usage" to usage,
            "permisison" to permission,
            "permission-message" to permission,
            "aliases" to aliases
        ).filterValues { it != null }
    }
}

// @JsonPropertyOrder("description", "default", "children")
open class Permission @Inject constructor(@Transient val name: String) {
    var description: String? = null

    // @SerialName("default")
    var defaults: String? = null
    var children = emptyMap<String, Boolean>()

    fun serialize(): Map<String, Any?> {
        return mapOf(
            "description" to description,
            "default" to defaults,
            "children" to children
        ).filterValues { it != null }
    }
}

enum class SpigotRepositories(val address: String) {
    // @SerialName("purpurmc"), purpur
    PURPUR_MC("https://repo.purpurmc.org/snapshots"),

    // @SerialName("spigotmc"), spigot
    SPIGOT_MC("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"),

    // @SerialName("papermc"), paper
    PAPER_MC("https://repo.papermc.io/repository/maven-public/"),
    PROTOCOL_LIB("https://repo.dmulloy2.net/nexus/repository/public/"),

    // @SerialName("enginehub")
    ENGINE_HUB("https://maven.enginehub.org/repo/"),

    // @SerialName("codemc"), bstats
    CODE_MC("https://repo.codemc.org/repository/maven-public/"),

    // essentialsX
    ENDER_ZONE("https://ci.ender.zone/plugin/repository/everything/"),

    // banManager
    FROSTCAST("https://ci.frostcast.net/plugin/repository/everything")
}

object SpigotDependencies {
    val PURPUR = Dependency(
        "org.purpurmc.purpur",
        "purpur-api",
        "1.18.1-R0.1-SNAPSHOT",
        false,
        VersionModifier.SPIGOT_ADJUSTER
    )
    val SPIGOT = Dependency(
        "org.spigotmc",
        "spigot-api",
        "1.18.1-R0.1-SNAPSHOT",
        false,
        VersionModifier.SPIGOT_ADJUSTER
    )
    val SPIGOT_ALL = Dependency(SPIGOT, name = "spigot", isLocal = true)
    val MINECRAFT_SERVER = Dependency(
        SPIGOT.group,
        "minecraft-server",
        "1.18.1-SNAPSHOT",
        true,
        VersionModifier.SNAPSHOT_APPENDER
    )
    val PAPER = Dependency(SPIGOT, "io.papermc.paper", "paper-api")
    val PAPER_ALL = Dependency(PAPER, name = "paper", isLocal = true)
    val BUKKIT = Dependency(SPIGOT, group = "org.bukkit", name = "bukkit", isLocal = true)

    // @SerialName("craftbukkit")
    val CRAFT_BUKKIT = Dependency(BUKKIT, name = "craftbukkit", isLocal = true)
    val PROTOCOL_LIB = Dependency(
        "com.comphenix.protocol",
        "ProtocolLib",
        "4.5.1"
    )
    val VAULT = Dependency(
        "com.github.MilkBowl",
        "VaultAPI",
        "1.7"
    )
    val VAULT_ALL = Dependency(VAULT, name = "Vault", version = "1.7.3")
    val LUCK_PERMS = Dependency(
        "net.luckperms",
        "api",
        "5.1"
    )

    // @SerialName("worldedit")
    val WORLD_EDIT = Dependency(
        "com.sk89q.worldedit",
        "worldedit-bukkit",
        "7.1.0"
    )

    // @SerialName("worldguard")
    val WORLD_GUARD = Dependency(
        "com.sk89q.worldguard",
        "worldguard-bukkit",
        "7.0.3"
    )
    val ESSENTIALS_X = Dependency(
        "net.ess3",
        "EssentialsX",
        "2.17.2"
    )
    val BAN_MANAGER = Dependency(
        "me.confuser.banmanager",
        "BanManagerBukkit",
        "7.3.0-SNAPSHOT"
    )

    // @SerialName("commandhelper")
    val COMMAND_HELPER = Dependency(
        "com.sk89q",
        "commandhelper",
        "3.3.4-SNAPSHOT"
    )
    val B_STATS = Dependency(
        "org.bstats",
        "bstats-bukkit",
        "1.7"
    )
    val B_STATS_LITE = Dependency(B_STATS, name = "bstats-bukkit-lite")
}
