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

package io.typst.spigradle.nukkit

import io.typst.spigradle.caseKebabToPascal
import io.typst.spigradle.spigot.Command
import io.typst.spigradle.spigot.Load
import io.typst.spigradle.spigot.Permission
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

/**
 * Nukkit configuration for the 'plugin.yml' description, and debug settings.
 *
 * Groovy Example:
 * ```groovy
 * spigot {
 *   authors 'Me'
 *   depends 'ProtocolLib', 'Vault'
 *   api '1.0.5'
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
 *   api = listOf("1.0.5")
 *   load = Load.STARTUP
 *   commands {
 *     register("give") {
 *       aliases = listOf("giv", "i")
 *       description = "Give command."
 *       permission = "test.foo"
 *       permissionMessage = "You do not have the permission!"
 *       usage = "/<command> [item] [amount]"
 *     }
 *   }
 *   permissions {
 *     register("test.foo") {
 *       description = "Allows foo command"
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
 * See: [https://github.com/NukkitX/ExamplePlugin/blob/master/src/main/resources/plugin.yml#L1]
 */
//@JsonPropertyOrder(
//        "main", "name", "version", "description", "website",
//        "authors", "api", "load", "prefix", "depend",
//        "softdepend", "loadbefore", "commands", "permissions"
//)
open class NukkitExtension(project: Project) {
    var main: Property<String> = project.objects.property()
    var name: Property<String> =
        project.objects.property<String>().convention(project.provider { project.name.caseKebabToPascal() })
    var version: Property<String> =
        project.objects.property<String>().convention(project.provider { project.version.toString() })
    var description: Property<String> =
        project.objects.property<String>().convention(project.provider { project.description })
    var website: String? = null
    var authors: List<String> = emptyList()
    var api: List<String> = emptyList()
    var load: Load? = null
    var prefix: String? = null

    //    @SerialName("depend")
    var depends: List<String> = emptyList()

    //    @SerialName("softdepend")
    var softDepends: List<String> = emptyList()

    //    @SerialName("loadbefore")
    var loadBefore: List<String> = emptyList()

    val commands: NamedDomainObjectContainer<Command> = project.container { project.objects.newInstance(it) }
    val permissions: NamedDomainObjectContainer<Permission> = project.container { project.objects.newInstance(it) }

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

    fun website(website: String) {
        this.website = website
    }

    fun authors(authors: Array<String>) {
        this.authors = authors.toList()
    }

    fun api(vararg apis: String) {
        this.api = apis.toList()
    }

    fun load(load: Load) {
        this.load = load
    }

    fun prefix(prefix: String) {
        this.prefix = prefix
    }

    fun depends(vararg depends: String) {
        this.depends = depends.toList()
    }

    fun softDepends(vararg softDepends: String) {
        this.softDepends = softDepends.toList()
    }

    fun loadBefore(vararg loadBefore: String) {
        this.loadBefore = loadBefore.toList()
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
            "api" to api.ifEmpty { null },
            "load" to load?.label,
            "prefix" to prefix,
            "depend" to depends.ifEmpty { null },
            "softdepend" to softDepends.ifEmpty { null },
            "loadbefore" to loadBefore.ifEmpty { null },
            "commands" to commands.map {
                it.serialize()
            }.ifEmpty { null },
            "permissions" to permissions.map {
                it.serialize()
            }.ifEmpty { null },
        ).filterValues {
            it != null
        }
    }
}
