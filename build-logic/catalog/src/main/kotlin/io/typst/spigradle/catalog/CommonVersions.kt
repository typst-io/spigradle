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

enum class CommonVersions(val version: Version) {
    LOMBOK(Version("1.18.38", "lombok")),
    AHOCORASICK(Version("0.6.3", "ahocorasick")),
    TYPST_VIEW(Version("10.1.1", "typst-view")),
    TYPST_COMMAND(Version("3.1.7", "typst-command")),
    TYPST_INVENTORY(Version("2.7.6", "typst-inventory")),
    JOOQ(Version("3.19.29", "jooq")),
    HIKARI_CP(Version("7.0.2", "hikariCP")),
    FLYWAY(Version("11.19.0", "flyway")),
    BUKKIT_KOTLIN_SERIALIZATION(Version("4.0.3", "typst-bukkitKotlinSerialization")),
    KOTLINX_SERIALIZATION(Version("1.9.0", "kotlinxSerialization")),
    KAML(Version("0.104.0", "kaml")),
    JUNIT(Version("6.0.1", "junit")),
    MOCKITO(Version("5.21.0", "mockito")),
    ASSERTJ(Version("3.27.6", "assertj")),
    MYSQL_DRIVER(Version("9.5.0", "mysql-driver")),
}