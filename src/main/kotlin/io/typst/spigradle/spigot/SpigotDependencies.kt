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

import io.typst.spigradle.Dependency
import io.typst.spigradle.VersionModifier

enum class SpigotDependencies(
    val group: String,
    val publicName: String,
    val version: String,
    val alias: String,
    val local: Boolean = false,
    val versionModifier: (String) -> String = { it },
) {
    PURPUR(
        "org.purpurmc.purpur",
        "purpur-api",
        "1.21.8-R0.1-SNAPSHOT",
        "purpur",
        false,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    SPIGOT_API(
        "org.spigotmc",
        "spigot-api",
        "1.21.8-R0.1-SNAPSHOT",
        "spigot",
        false,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    SPIGOT(
        "org.spigotmc",
        "spigot",
        "1.21.8-R0.1-SNAPSHOT",
        "spigotAll",
        true,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    MINECRAFT_SERVER(
        SPIGOT.group,
        "minecraft-server",
        "1.21.8-SNAPSHOT",
        "minecraftServer",
        true,
        VersionModifier.SNAPSHOT_APPENDER
    ),
    PAPER_API(
        "io.papermc.paper",
        "paper-api",
        "1.21.8-R0.1-SNAPSHOT",
        "paper",
        versionModifier = VersionModifier.SPIGOT_ADJUSTER
    ),
    BUKKIT(
        "org.bukkit",
        "bukkit",
        "1.21.8-R0.1-SNAPSHOT",
        "bukkit",
        true,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    CRAFTBUKKIT(
        "org.bukkit",
        "craftbukkit",
        "1.21.8-R0.1-SNAPSHOT",
        "craftbukkit",
        true,
        VersionModifier.SPIGOT_ADJUSTER
    ),
    PROTOCOL_LIB(
        "com.comphenix.protocol",
        "ProtocolLib",
        "5.3.0",
        "protocolLib"
    ),
    VAULT_API(
        "com.github.MilkBowl",
        "VaultAPI",
        "1.7",
        "vault"
    ),
    LUCK_PERMS(
        "net.luckperms",
        "api",
        "5.5.9",
        "luckperms",
    ),
    WORLDEDIT(
        "com.sk89q.worldedit",
        "worldedit-bukkit",
        "7.3.15",
        "worldedit",
    ),
    WORLDGUARD(
        "com.sk89q.worldguard",
        "worldguard-bukkit",
        "7.0.14",
        "worldguard",
    ),
    ESSENTIALS_X(
        "net.ess3",
        "EssentialsX",
        "2.21.1",
        "essentialsX",
    ),
    BAN_MANAGER(
        "me.confuser.banmanager",
        "BanManagerBukkit",
        "7.9.0-SNAPSHOT",
        "banManager",
    ),
    COMMANDHELPER(
        "com.sk89q",
        "commandhelper",
        "3.3.5-SNAPSHOT",
        "commandHelper",
    ),
    B_STATS(
        "org.bstats",
        "bstats-bukkit",
        "3.0.2",
        "bStats",
    ),
    ;

    fun toDependency(): Dependency {
        return Dependency(group, publicName, version, local, versionModifier)
    }

    fun format(version: String? = null): String {
        return toDependency().format(version)
    }
}
