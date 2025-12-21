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
enum class PaperDependencies(
    val dependency: Dependency,
) {
    SPIGOT_API(
        Dependency(
            "org.spigotmc",
            "spigot-api",
            PaperVersions.PAPER_API.version,
            "spigot-api",
            tags = Dependency.SPIGOT_VERSION_TAGS,
        )
    ),
    SPIGOT(
        Dependency(
            "org.spigotmc",
            "spigot",
            PaperVersions.PAPER_API.version,
            "spigot-server",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
        )
    ),
    PURPUR(
        Dependency(
            "org.purpurmc.purpur",
            "purpur-api",
            PaperVersions.PAPER_API.version,
            "purpur-api",
            tags = Dependency.SPIGOT_VERSION_TAGS,
        )
    ),
    PAPER_API(
        Dependency(
            "io.papermc.paper",
            "paper-api",
            PaperVersions.PAPER_API.version,
            "paper-api",
            tags = Dependency.SPIGOT_VERSION_TAGS,
        )
    ),
    MINECRAFT_SERVER(
        Dependency(
            SPIGOT.dependency.group,
            "minecraft-server",
            PaperVersions.PAPER_SERVER.version,
            "minecraftServer",
            true,
            tags = Dependency.SNAPSHOT_TAG,
        )
    ),
    BUKKIT(
        Dependency(
            "org.bukkit",
            "bukkit",
            PaperVersions.PAPER_API.version,
            "bukkit",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
        )
    ),
    CRAFTBUKKIT(
        Dependency(
            "org.bukkit",
            "craftbukkit",
            PaperVersions.PAPER_API.version,
            "craftbukkit",
            true,
            tags = Dependency.SPIGOT_VERSION_TAGS,
        )
    ),
    PROTOCOL_LIB(
        Dependency(
            "net.dmulloy2",
            "ProtocolLib",
            PaperVersions.PROTOCOL_LIB.version,
            "protocolLib"
        )
    ),
    VAULT_API(
        Dependency(
            "com.github.MilkBowl",
            "VaultAPI",
            PaperVersions.VAULT_API.version,
            "vault-api"
        )
    ),
    LUCK_PERMS(
        Dependency(
            "net.luckperms",
            "api",
            PaperVersions.LUCKPERMS.version,
            "luckperms-api",
        )
    ),
    WORLDEDIT(
        Dependency(
            "com.sk89q.worldedit",
            "worldedit-bukkit",
            PaperVersions.WORLD_EDIT.version,
            "worldedit"
        )
    ),
    WORLDGUARD(
        Dependency(
            "com.sk89q.worldguard",
            "worldguard-bukkit",
            PaperVersions.WORLD_GUARD.version,
            "worldguard",
        )
    ),
    ESSENTIALS_X(
        Dependency(
            "net.essentialsx",
            "EssentialsX",
            PaperVersions.ESSENTIALS_X.version,
            "essentialsX",
        )
    ),
    BAN_MANAGER(
        Dependency(
            "me.confuser.banmanager",
            "BanManagerBukkit",
            PaperVersions.BAN_MANAGER.version,
            "banManager",
        )
    ),
    COMMAND_HELPER(
        Dependency(
            "com.sk89q",
            "commandhelper",
            PaperVersions.COMMAND_HELPER.version,
            "commandhelper",
        )
    ),
    B_STATS(
        Dependency(
            "org.bstats",
            "bstats-bukkit",
            PaperVersions.B_STATS.version,
            "bstats",
        )
    ),
    MOCK_BUKKIT(
        Dependency(
            "org.mockbukkit.mockbukkit",
            "mockbukkit-v1.21",
            PaperVersions.MOCK_BUKKIT.version,
            "mockbukkit",
        )
    ),
    PLACEHOLDER_API(
        Dependency(
            "me.clip",
            "placeholderapi",
            PaperVersions.PLACEHOLDER_API.version,
            "placeholderapi",
        )
    ),
    ACF_PAPER(
        Dependency(
            "co.aikar",
            "acf-paper",
            PaperVersions.AFC.version,
            "acf-paper",
        )
    ),
    ;
}
