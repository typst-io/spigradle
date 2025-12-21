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

import io.typst.spigradle.asCamelCase
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Spigot configuration for the `plugin.yml` description.
 *
 * Groovy example:
 * ```groovy
 * spigot {
 *   authors = ['Me', 'Someone']
 *   depend = ['ProtocolLib', 'Vault']
 *   softDepend = ['MySoftLib']
 *   apiVersion = '1.15'
 *   load = 'STARTUP'
 *   commands {
 *     register('give') {
 *       aliases = ['giv', 'i']
 *       description = 'Give command.'
 *       permission = 'test.foo'
 *       permissionMessage = 'You do not have permission!'
 *       usage = '/<command> [item] [amount]'
 *     }
 *   }
 *   permissions {
 *     register('test.foo') {
 *       description = 'Allows the foo command'
 *       defaults = 'true'
 *     }
 *     register('test.*') {
 *       description = 'Wildcard permission'
 *       defaults = 'op'
 *       children = ['test.foo': true]
 *     }
 *   }
 * }
 * ```
 *
 * Kotlin example:
 * ```kotlin
 * spigot {
 *   authors = listOf("Me")
 *   depend = listOf("ProtocolLib", "Vault")
 *   softDepend = listOf("MySoftLib")
 *   apiVersion = "1.15"
 *   load = "STARTUP"
 *   commands {
 *     register("give") {
 *       aliases = listOf("giv", "i")
 *       description = "Give command."
 *       permission = "test.foo"
 *       permissionMessage = "You do not have permission!"
 *       usage = "/<command> [item] [amount]"
 *     }
 *   }
 *   permissions {
 *     register("test.foo") {
 *       description = "Allows the foo command"
 *       defaults = "true"
 *     }
 *     register("test.*") {
 *       description = "Wildcard permission"
 *       defaults = "op"
 *       children = mapOf("test.foo" to true)
 *     }
 *   }
 * }
 * ```
 *
 * See also: https://www.spigotmc.org/wiki/plugin-yml/
 *
 * @property main Defaults to main class detected by the `detectSpigotEntrypoints` task ([io.typst.spigradle.SubclassDetection])
 * @property name Defaults to `project.name.caseKebabToPascal()`
 * @property version Defaults to `project.version`
 * @property description Defaults to `project.description`
 */
abstract class SpigotExtension @Inject constructor(private val project: Project) {

    /**
     * The name of main class that extends [org.bukkit.plugin.java.JavaPlugin].
     *
     * Defaults to the class that auto-detected by [io.typst.spigradle.SubclassDetection]
     *
     * See: [Spigot plugin.yml Reference](https://www.spigotmc.org/wiki/plugin-yml/)
     */
    abstract val main: Property<String>

    /**
     * The name of your plugin.
     *
     * Defaults to [Project.getName].
     *
     * See: [Spigot plugin.yml Reference](https://www.spigotmc.org/wiki/plugin-yml/)
     */
    abstract val name: Property<String>

    /**
     * The version of your plugin.
     *
     * Defaults to [Project.getVersion]
     *
     * See: [Spigot plugin.yml Reference](https://www.spigotmc.org/wiki/plugin-yml/)
     */
    abstract val version: Property<String>

    abstract val description: Property<String>

    abstract val website: Property<String>
    abstract val authors: ListProperty<String>
    abstract val apiVersion: Property<String>

    /**
     * The load order of your plugin.
     *
     * Available values:
     * - POSTWORLD(default)
     * - STARTUP
     *
     * See: [Spigot plugin.yml Reference](https://www.spigotmc.org/wiki/plugin-yml/)
     */
    abstract val load: Property<String>
    abstract val prefix: Property<String>
    abstract val depend: ListProperty<String>
    abstract val softDepend: ListProperty<String>
    abstract val loadBefore: ListProperty<String>

    /**
     * Runtime libraries of your plugin that will be loaded without shading(fat-jar) by Spigot 1.17 or higher.
     *
     * Example: `com.squareup.okhttp3:okhttp:4.9.0`
     *
     * See also: [Spigot & BungeeCord 1.17](https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317)
     */
    abstract val libraries: ListProperty<String>

    /**
     * DSL container for the [commands] configuration.
     *
     * Groovy Example:
     * ```groovy
     * commands {
     *   give {
     *     aliases 'giv', 'i'
     *     description 'Give command.'
     *   }
     * }
     * ```
     *
     * Kotlin Example:
     *
     * ```kotlin
     * commands {
     *   register("give") {
     *     aliases = listOf("giv", "i")
     *     description = "Give command."
     *   }
     * }
     * ```
     *
     * See: [Spigot plugin.yml Reference](https://www.spigotmc.org/wiki/plugin-yml/)
     */
    abstract val commands: NamedDomainObjectContainer<Command>

    /**
     * DSL container for the [permissions] configuration.
     *
     * Groovy Example:
     * ```groovy
     * permissions {
     *   'test.foo' {
     *     description 'Allows foo command.'
     *     default 'true'
     *   }
     * }
     * ```
     *
     * Kotiln Example:
     * ```kotlin
     * permissions {
     *   register("test.foo") {
     *     description = "Allows foo command."
     *     default = "true"
     *   }
     * }
     * ```
     *
     * See: [Spigot plugin.yml Reference](https://www.spigotmc.org/wiki/plugin-yml/)
     */
    abstract val permissions: NamedDomainObjectContainer<Permission>

    init {
        name.convention(project.provider { project.name.asCamelCase(true) })
        version.convention(project.provider { project.version.toString() })
        description.convention(project.provider { project.description })
    }

    val POSTWORLD: String = "POSTWORLD"
    val STARTUP: String = "STARTUP"

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "main" to main.orNull, // to retain order (file fallback property)
            "name" to name.orNull,
            "version" to version.orNull,
            "description" to description.orNull,
            "website" to website.orNull,
            "authors" to authors.orNull?.ifEmpty { null },
            "api-version" to apiVersion.orNull,
            "load" to load.orNull,
            "prefix" to prefix.orNull,
            "depend" to depend.orNull?.ifEmpty { null },
            "softdepend" to softDepend.orNull?.ifEmpty { null },
            "loadbefore" to loadBefore.orNull?.ifEmpty { null },
            "libraries" to libraries.orNull?.ifEmpty { null },
            "commands" to commands.toList().associate {
                it.name to it.toMap()
            }.ifEmpty { null },
            "permissions" to permissions.toList().associate {
                it.name to it.toMap()
            }.ifEmpty { null },
        )
    }
}
