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

import io.typst.spigradle.caseKebabToPascal
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

/**
 * Spigot configuration for the `plugin.yml` description.
 *
 * Groovy example:
 * ```groovy
 * spigot {
 *   authors 'Me', 'Someone'
 *   depends 'ProtocolLib', 'Vault'
 *   softDepends 'MySoftLib'
 *   apiVersion '1.15'
 *   load STARTUP
 *   commands {
 *     give {
 *       aliases 'giv', 'i'
 *       description 'Give command.'
 *       permission 'test.foo'
 *       permissionMessage 'You do not have permission!'
 *       usage '/<command> [item] [amount]'
 *     }
 *   }
 *   permissions {
 *     'test.foo' {
 *       description 'Allows the foo command'
 *       defaults 'true'
 *     }
 *     'test.*' {
 *       description 'Wildcard permission'
 *       defaults 'op'
 *       children = ['test.foo': true]
 *     }
 *   }
 *   debug {
 *     eula = true
 *   }
 * }
 * ```
 *
 * Kotlin example:
 * ```kotlin
 * import io.typst.spigradle.Load
 *
 * spigot {
 *   authors = "Me"
 *   depends = listOf("ProtocolLib", "Vault")
 *   softDepends = listOf("MySoftLib")
 *   apiVersion = "1.15"
 *   load = Load.STARTUP
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
 * @property main Defaults to main class detected by the `detectSpigotMain` task ([io.typst.spigradle.SubclassDetection])
 * @property name Defaults to `project.name.caseKebabToPascal()`
 * @property version Defaults to `project.version`
 * @property description Defaults to `project.description
 */
//@JsonPropertyOrder(
//    "main", "name", "version", "description", "website",
//    "authors", "api-version", "load", "prefix", "depend",
//    "softdepend", "loadbefore", "libraries", "commands",
//    "permissions"
//)
open class SpigotExtension(project: Project) {
    /**
     * The name of main class that extends [org.bukkit.plugin.java.JavaPlugin].
     *
     * Defaults to the class that auto-detected by [io.typst.spigradle.SubclassDetection]
     *
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     */
    var main: Property<String> = project.objects.property()

    /**
     * The name of your plugin.
     *
     * Defaults to [Project.getName].
     *
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     */
    var name: Property<String> =
        project.objects.property<String>().convention(project.provider { project.name.caseKebabToPascal() })

    /**
     * The version of your plugin.
     *
     * Defaults to [Project.getVersion]
     *
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     */
    var version: Property<String> =
        project.objects.property<String>().convention(project.provider { project.version.toString() })
    var description: Property<String> =
        project.objects.property<String>().convention(project.provider { project.description })

    var website: String? = null
    var authors: List<String> = emptyList()

    // @SerialName("api-version")
    var apiVersion: String? = null

    /**
     * The load order of your plugin.
     *
     * Groovy Example:
     * ```groovy
     * spigot {
     *   load = STARTUP // or POSTWORLD
     * }
     * ```
     *
     * Kotlin Example:
     *
     * ```kotlin
     * import io.typst.spigradle.Load
     *
     * spigot {
     *   load = Load.STARTUP // or Load.POST_WORLD
     * }
     *
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     * ```
     */
    var load: Load? = null
    var prefix: String? = null

    //    @SerialName("depend")
    var depends: List<String> = emptyList()

    //    @SerialName("softdepend")
    var softDepends: List<String> = emptyList()

    //    @SerialName("loadbefore")
    var loadBefore: List<String> = emptyList()

    /**
     * Runtime libraries of your plugin that will be loaded without shading(fat-jar) by Spigot 1.17 or higher.
     *
     * Example: `com.squareup.okhttp3:okhttp:4.9.0`
     *
     * See also: [Spigot & BungeeCord 1.17](https://www.spigotmc.org/threads/spigot-bungeecord-1-17.510208/#post-4184317)
     */
    var libraries: List<String> = emptyList()

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
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     */
    val commands: NamedDomainObjectContainer<Command> = project.run { container { objects.newInstance(it) } }

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
     * See: [https://www.spigotmc.org/wiki/plugin-yml/]
     */
    val permissions: NamedDomainObjectContainer<Permission> = project.run { container { objects.newInstance(it) } }

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

    /**
     * Groovy DSL helper for the [website] property.
     */
    fun website(xs: String) {
        website = xs
    }

    /**
     * Groovy DSL helper for the [authors] configuration.
     */
    fun authors(vararg authors: String) {
        this.authors = authors.toList()
    }

    /**
     * Groovy DSL helper for the [apiVersion] property.
     */
    fun apiVersion(xs: String) {
        apiVersion = xs
    }

    /**
     * Groovy DSL helper for the [load] property.
     */
    fun load(x: Load) {
        load = x
    }

    /**
     * Groovy DSL helper for the [prefix] property.
     */
    fun prefix(xs: String) {
        prefix = xs
    }

    /**
     * Groovy DSL helper for the [depends] configuration.
     */
    fun depends(vararg depends: String) {
        this.depends = depends.toList()
    }

    /**
     * Groovy DSL helper for the [softDepends] configuration.
     */
    fun softDepends(vararg softDepends: String) {
        this.softDepends = softDepends.toList()
    }

    /**
     * Groovy DSL helper for the [loadBefore] configuration.
     */
    fun loadBefore(vararg loadBefore: String) {
        this.loadBefore = loadBefore.toList()
    }

    /**
     * Groovy DSL helper for the [libraries] configuration.
     */
    fun libraries(vararg libraries: String) {
        this.libraries = libraries.toList()
    }

    /**
     * DSL helper for [commands] configuration.
     */
    fun commands(configure: Action<NamedDomainObjectContainer<Command>>) {
        configure.execute(commands)
    }

    /**
     * DSL helper for [permissions] configuration.
     */
    fun permissions(configure: Action<NamedDomainObjectContainer<Permission>>) {
        configure.execute(permissions)
    }

    fun encodeToMap(): Map<String, Any?> {
        return linkedMapOf(
            "main" to main.orNull,
            "name" to name.orNull,
            "version" to version.orNull,
            "description" to description.orNull,
            "website" to website,
            "authors" to authors.ifEmpty { null },
            "api-version" to apiVersion,
            "load" to load?.label,
            "prefix" to prefix,
            "depend" to depends.ifEmpty { null },
            "softdepend" to softDepends.ifEmpty { null },
            "loadbefore" to loadBefore.ifEmpty { null },
            "libraries" to libraries.ifEmpty { null },
            "commands" to commands.toList().associate {
                it.name to it.serialize()
            }.ifEmpty { null },
            "permissions" to permissions.toList().associate {
                it.name to it.serialize()
            }.ifEmpty { null },
        ).filterValues {
            it != null
        }
    }
}
