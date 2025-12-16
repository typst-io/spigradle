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
enum class SpigotDependencies(
    val dependency: Dependency,
) {
    SPIGOT_API(
        Dependency(
            "org.spigotmc",
            "spigot-api",
            "1.21.8-R0.1-SNAPSHOT",
            "spigot-api",
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    SPIGOT(
        Dependency(
            "org.spigotmc",
            "spigot",
            "1.21.8-R0.1-SNAPSHOT",
            "spigot-all",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    PURPUR(
        Dependency(
            "org.purpurmc.purpur",
            "purpur-api",
            "1.21.8-R0.1-SNAPSHOT",
            "purpur-api",
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    PAPER_API(
        Dependency(
            "io.papermc.paper",
            "paper-api",
            "1.21.8-R0.1-SNAPSHOT",
            "paper-api",
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    MINECRAFT_SERVER(
        Dependency(
            SPIGOT.dependency.group,
            "minecraft-server",
            "1.21.8-SNAPSHOT",
            "minecraftServer",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    BUKKIT(
        Dependency(
            "org.bukkit",
            "bukkit",
            "1.21.8-R0.1-SNAPSHOT",
            "bukkit",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    CRAFTBUKKIT(
        Dependency(
            "org.bukkit",
            "craftbukkit",
            "1.21.8-R0.1-SNAPSHOT",
            "craftbukkit",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
            versionRef = "spigot",
        )
    ),
    PROTOCOL_LIB(
        Dependency(
            "net.dmulloy2",
            "ProtocolLib",
            "5.4.0",
            "protocolLib"
        )
    ),
    VAULT_API(
        Dependency(
            "com.github.MilkBowl",
            "VaultAPI",
            "1.7",
            "vault-api"
        )
    ),
    LUCK_PERMS(
        Dependency(
            "net.luckperms",
            "api",
            "5.5",
            "luckperms-api",
        )
    ),
    WORLDEDIT(
        Dependency(
            "com.sk89q.worldedit",
            "worldedit-bukkit",
            "7.3.17",
            "worldedit",
        )
    ),
    WORLDGUARD(
        Dependency(
            "com.sk89q.worldguard",
            "worldguard-bukkit",
            "7.0.14",
            "worldguard",
        )
    ),
    ESSENTIALS_X(
        Dependency(
            "net.essentialsx",
            "EssentialsX",
            "2.21.1",
            "essentialsX",
        )
    ),
    BAN_MANAGER(
        Dependency(
            "me.confuser.banmanager",
            "BanManagerBukkit",
            "7.7.0-SNAPSHOT",
            "banManager",
        )
    ),
    COMMANDHELPER(
        Dependency(
            "com.sk89q",
            "commandhelper",
            "3.3.4-SNAPSHOT",
            "commandhelper",
        )
    ),
    B_STATS(
        Dependency(
            "org.bstats",
            "bstats-bukkit",
            "3.0.2",
            "bstats",
        )
    ),
    MOCK_BUKKIT(
        Dependency(
            "org.mockbukkit.mockbukkit",
            "mockbukkit-v1.21",
            "4.98.0",
            "mockbukkit"
        )
    ),
    SPIGRADLOE_PLUGIN(
        Dependency(
            "io.typst.spigradle",
            "io.typst.spigradle.gradle.plugin",
            "4.0.0",
            "spigradleSpigot-plugin",
            versionRef = "spigradle",
            isLocal = true,
        )
    )
    ;

    fun format(version: String? = null): String {
        return dependency.format(version)
    }
}
