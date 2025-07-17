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
            "aliases" to aliases.ifEmpty { null }
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
            "children" to children.ifEmpty { null }
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

enum class SpigotDependencies(
    val group: String,
    val publicName: String,
    val version: String,
    val local: Boolean = false,
    val versionModifier: (String) -> String = { it },
) {
    PURPUR(
        "org.purpurmc.purpur",
        "purpur-api",
        "1.18.1-R0.1-SNAPSHOT",
        false,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    SPIGOT_API(
        "org.spigotmc",
        "spigot-api",
        "1.18.1-R0.1-SNAPSHOT",
        false,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    SPIGOT(
        "org.spigotmc",
        "spigot",
        "1.18.1-R0.1-SNAPSHOT",
        true,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    MINECRAFT_SERVER(
        SPIGOT.group,
        "minecraft-server",
        "1.18.1-SNAPSHOT",
        true,
        VersionModifier.SNAPSHOT_APPENDER
    ),
    PAPER_API(
        "io.papermc.paper",
        "paper-api",
        "1.18.1-R0.1-SNAPSHOT",
        versionModifier = VersionModifier.SPIGOT_ADJUSTER
    ),
    BUKKIT(
        "org.bukkit",
        "bukkit",
        "1.18.1-R0.1-SNAPSHOT",
        true,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    CRAFTBUKKIT(
        "org.bukkit",
        "craftbukkit",
        "1.18.1-R0.1-SNAPSHOT",
        true,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    PROTOCOL_LIB(
        "com.comphenix.protocol",
        "ProtocolLib",
        "4.5.1"
    ),
    VAULT_API(
        "com.github.MilkBowl",
        "VaultAPI",
        "1.7"
    ),
    LUCK_PERMS(
        "net.luckperms",
        "api",
        "5.1"
    ),
    WORLDEDIT(
        "com.sk89q.worldedit",
        "worldedit-bukkit",
        "7.1.0"
    ),
    WORLDGUARD(
        "com.sk89q.worldguard",
        "worldguard-bukkit",
        "7.0.3"
    ),
    ESSENTIALS_X(
        "net.ess3",
        "EssentialsX",
        "2.17.2"
    ),
    BAN_MANAGER(
        "me.confuser.banmanager",
        "BanManagerBukkit",
        "7.3.0-SNAPSHOT"
    ),
    COMMANDHELPER(
        "com.sk89q",
        "commandhelper",
        "3.3.4-SNAPSHOT"
    ),
    B_STATS(
        "org.bstats",
        "bstats-bukkit",
        "1.7"
    ),
    ;

    fun toDependency(): Dependency {
        return Dependency(group, publicName, version, local, versionModifier)
    }

    fun format(version: String?): String {
        return toDependency().format(version)
    }
}
