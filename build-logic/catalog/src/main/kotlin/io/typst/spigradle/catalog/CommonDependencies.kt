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
enum class CommonDependencies(
    val dependency: Dependency,
) {
    LOMBOK(
        Dependency(
            "org.projectlombok",
            "lombok",
            "1.18.38",
            "lombok"
        )
    ),
    AHOCORASICK(
        Dependency(
            "org.ahocorasick",
            "ahocorasick",
            "0.6.3",
            "ahocorasick"
        )
    ),
    VIEW_BUKKIT_KOTLIN(
        Dependency(
            "io.typst",
            "view-bukkit-kotlin",
            "10.0.5",
            "typst-view-bukkitKotlin",
            versionRef = "typst-view",
        )
    ),
    COMMAND_KOTLIN(
        Dependency(
            "io.typst",
            "command-kotlin",
            "3.1.6",
            "typst-command-kotlin",
            versionRef = "typst-command"
        )
    ),
    COMMAND_BUKKIT(
        Dependency(
            "io.typst",
            "command-bukkit",
            "3.1.6",
            "typst-command-bukkit",
            versionRef = "typst-command"
        )
    ),
    INVENTORY_BUKKIT_KOTLIN(
        Dependency(
            "io.typst",
            "inventory-bukkit-kotlin",
            "2.7.5",
            "typst-inventory-bukkitKotlin",
            versionRef = "typst-inventory"
        )
    ),
    JOOQ(
        Dependency(
            "org.jooq",
            "jooq",
            "3.19.29",
            alias = "jooq-core",
            versionRef = "jooq"
        )
    ),
    JOOQ_META(
        Dependency(
            "org.jooq",
            "jooq-meta",
            "3.19.29",
            alias = "jooq-meta",
            versionRef = "jooq"
        )
    ),
    HIKARI_CP(
        Dependency(
            "com.zaxxer",
            "HikariCP",
            "7.0.2",
            alias = "hikariCP",
            versionRef = "hikariCP"
        )
    ),
    FLYWAY_CORE(
        Dependency(
            "org.flywaydb",
            "flyway-core",
            "11.19.0",
            alias = "flyway-core",
            versionRef = "flyway"
        )
    ),
    FLYWAY_MYSQL(
        Dependency(
            "org.flywaydb",
            "flyway-mysql",
            "11.19.0",
            alias = "flyway-mysql",
            versionRef = "flyway"
        )
    ),
    IDEA_EXT_PLUGIN(
        Dependency(
            "org.jetbrains.gradle.plugin.idea-ext",
            "gradle-idea-ext",
            "1.3",
            "ideaExt-plugin",
            versionRef = "ideaExt",
            isLocal = true,
        )
    ),
    BUKKIT_KOTLIN_SERIALIZATION(
        Dependency(
            "io.typst",
            "bukkit-kotlin-serialization",
            "4.0.0",
            "typst.bukkitKotlinSerialization"
        )
    ),
    KOTLINX_SERIALIZATION(
        Dependency(
            "org.jetbrains.kotlinx",
            "kotlinx-serialization-json",
            "1.9.0",
            "kotlinx-serialization-json",
        )
    ),
    KAML(
        Dependency(
            "com.charleskorn.kaml",
            "kaml-jvm",
            "0.104.0",
            alias = "kaml"
        )
    ),

    ;

    fun format(version: String?): String {
        return dependency.format(version)
    }
}
