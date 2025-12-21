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

enum class PaperVersions(val version: Version) {
    PAPER_API(Version("1.21.10-R0.1-SNAPSHOT", "spigot-api")),
    PAPER_SERVER(Version("1.21.10-SNAPSHOT", "spigot-server")),
    PROTOCOL_LIB(Version("5.4.0", "protocolLib")),
    VAULT_API(Version("1.7", "vault")),
    LUCKPERMS(Version("5.5", "luckperms")),
    WORLD_EDIT(Version("7.3.17", "worldEdit")),
    WORLD_GUARD(Version("7.0.14", "worldGuard")),
    ESSENTIALS_X(Version("2.21.1", "essentialsX")),
    BAN_MANAGER(Version("7.9.0", "banManager")),
    COMMAND_HELPER(Version("3.3.4-SNAPSHOT", "commandHelper")),
    B_STATS(Version("3.0.2", "bStats")),
    MOCK_BUKKIT(Version("4.98.0", "mockBukkit")),
    PLACEHOLDER_API(Version("2.11.7", "plcaeholderapi")),
}