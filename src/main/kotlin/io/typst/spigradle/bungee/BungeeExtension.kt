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

package io.typst.spigradle.bungee

import io.typst.spigradle.caseKebabToPascal
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

/**
 * Bungeecord configuration for the 'plugin.yml' description, and debug settings.
 *
 * Groovy Example:
 * ```groovy
 * spigot {
 *   author 'Me'
 *   depends 'ProtocolLib', 'Vault'
 *   debug {
 *     agentPort 5005
 *   }
 * }
 * ```
 *
 * Kotlin Example:
 * ```kotlin
 * import io.typst.spigradle.Load
 *
 * spigot {
 *   author = "Me"
 *   depends = listOf("ProtocolLib", "Vault")
 *   debug {
 *     agentPort = 5005
 *   }
 * }
 * ```
 *
 * See: [https://www.spigotmc.org/wiki/create-your-first-bungeecord-plugin-proxy-spigotmc/#making-it-load]
 */
open class BungeeExtension(project: Project) {
    var main: Property<String> = project.objects.property()
    var name: Property<String> =
        project.objects.property<String>().convention(project.provider { project.name.caseKebabToPascal() })
    var version: Property<String> =
        project.objects.property<String>().convention(project.provider { project.version.toString() })
    var description: Property<String> =
        project.objects.property<String>().convention(project.provider { project.description })
    var author: String? = null
    var depends: List<String> = emptyList()
    var softDepends: List<String> = emptyList()

    /**
     * Groovy DSL helper for the [main] lazy property.
     */
    fun main(xs: String) {
        main.set(xs)
    }

    /**
     * Groovy DSL helper for the [name] lazy property.
     */
    fun name(xs: String) {
        name.set(xs)
    }

    /**
     * Groovy DSL helper for the [version] lazy property.
     */
    fun version(xs: String) {
        version.set(xs)
    }

    /**
     * Groovy DSL helper for the [description] lazy property.
     */
    fun description(xs: String) {
        description.set(xs)
    }

    fun author(xs: String) {
        this.author = xs
    }

    fun depends(vararg depends: String) {
        this.depends = depends.toList()
    }

    fun softDepends(vararg softDepends: String) {
        this.softDepends = softDepends.toList()
    }

    fun encodeToMap(): Map<String, Any?> {
        return linkedMapOf(
            "main" to main.orNull,
            "name" to name.orNull,
            "version" to version.orNull,
            "description" to description.orNull,
            "author" to author,
            "depend" to depends.ifEmpty { null },
            "softdepend" to softDepends.ifEmpty { null }
        ).filterValues {
            it != null
        }
    }
}
