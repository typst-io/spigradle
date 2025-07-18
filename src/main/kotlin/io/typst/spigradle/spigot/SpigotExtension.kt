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
 * Spigot configuration for the 'plugin.yml' description, and debug settings.
 *
 * Groovy Example:
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
 *       permissionMessage 'You do not have the permission!'
 *       usage '/<command> [item] [amount]'
 *     }
 *   }
 *   permissions {
 *     'test.foo' {
 *       description 'Allows foo command'
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
 * Kotlin Example:
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
 *     create("give") {
 *       aliases = listOf("giv", "i")
 *       description = "Give command."
 *       permission = "test.foo"
 *       permissionMessage = "You do not have the permission!"
 *       usage = "/<command> [item] [amount]"
 *     }
 *   }
 *   permissions {
 *     create("test.foo") {
 *       description = "Allows foo command"
 *       defaults = "true"
 *     }
 *     create("test.*") {
 *       description = "Wildcard permission"
 *       defaults = "op"
 *       children = mapOf("test.foo" to true)
 *     }
 *   }
 * }
 * ```
 *
 * See: [https://www.spigotmc.org/wiki/plugin-yml/]
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
     * Defaults to the class that auto-detected by [io.typst.spigradle.SubclassDetection] or presented by [kr.entree.spigradle.annotations.Plugin].
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
    var name: Property<String> = project.objects.property<String>().convention(project.provider { project.name.caseKebabToPascal() })

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
     * Exclude libraries from the [libraries].
     *
     * This will be 'contains' matching with [libraries], it's ok just piece of keyword.
     *
     * Example:
     * - `okhttp`
     * - `com.squareup.okhttp3:okhttp`
     * - `com.squareup.okhttp3:okhttp:4.9.0`
     * - `*`
     *
     * See also: [libraries]
     */
    @Transient
    var excludeLibraries: List<String> = emptyList()

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
     *   create("give") {
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
     *   create("test.foo") {
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

    /**
     * Groovy DSL helper for the [authors] configuration.
     */
    fun authors(vararg authors: String) {
        this.authors = authors.toList()
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
     * Groovy DSL helper for the [excludeLibraries] configuration.
     */
    @Deprecated(
        message = "Use `excludeLibraries` instead",
        replaceWith = ReplaceWith("this.excludeLibraries(*dependencyNotations)")
    )
    fun shadowLibraries(vararg dependencyNotations: String) {
        this.excludeLibraries = listOf(*dependencyNotations)
    }

    /**
     * Groovy DSL helper for the [excludeLibraries] configuration.
     */
    fun excludeLibraries(vararg dependencyNotations: String) {
        this.excludeLibraries = listOf(*dependencyNotations)
    }
}
