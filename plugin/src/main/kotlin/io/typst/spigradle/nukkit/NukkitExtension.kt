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

import io.typst.spigradle.asCamelCase
import io.typst.spigradle.spigot.Command
import io.typst.spigradle.spigot.Permission
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * Nukkit configuration for the 'plugin.yml' description, and debug settings.
 *
 * Groovy Example:
 * ```groovy
 * nukkit {
 *   authors = 'Me'
 *   depend = ['ProtocolLib', 'Vault']
 *   api = '1.0.5'
 *   load = 'STARTUP'
 *   commands {
 *     register("give") {
 *       aliases = ['giv', 'i']
 *       description = 'Give command.'
 *       permission = 'test.foo'
 *       permissionMessage = 'You do not have the permission!'
 *       usage = '/<command> [item] [amount]'
 *     }
 *   }
 *   permissions {
 *     register('test.foo') {
 *       description = 'Allows foo command'
 *       defaults = 'true'
 *     }
 *     'test.*' {
 *       description = 'Wildcard permission'
 *       defaults = 'op'
 *       children = ['test.foo': true]
 *     }
 *   }
 * }
 * ```
 *
 * Kotlin Example:
 * ```kotlin
 * nukkit {
 *   authors = "Me"
 *   depend = listOf("ProtocolLib", "Vault")
 *   api = listOf("1.0.5")
 *   load = "STARTUP"
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
abstract class NukkitExtension @Inject constructor(private val project: Project) {
    abstract val main: Property<String>
    val name: Property<String> =
        project.objects.property<String>().convention(project.provider { project.name.asCamelCase(true) })
    val version: Property<String> =
        project.objects.property<String>().convention(project.provider { project.version.toString() })
    val description: Property<String> =
        project.objects.property<String>().convention(project.provider { project.description })
    abstract val website: Property<String>
    abstract val authors: ListProperty<String>
    abstract val api: ListProperty<String>
    abstract val load: Property<String>
    abstract val prefix: Property<String>
    abstract val depend: ListProperty<String>
    abstract val softDepend: ListProperty<String>
    abstract val loadBefore: ListProperty<String>

    abstract val commands: NamedDomainObjectContainer<Command>
    abstract val permissions: NamedDomainObjectContainer<Permission>

    fun toMap(): Map<String, Any> {
        return listOf(
            "main" to main.orNull,
            "name" to name.orNull,
            "version" to version.orNull,
            "description" to description.orNull,
            "website" to website.orNull,
            "authors" to authors.orNull?.ifEmpty { null },
            "api" to api.orNull?.ifEmpty { null },
            "load" to load.orNull,
            "prefix" to prefix.orNull,
            "depend" to depend.orNull?.ifEmpty { null },
            "softdepend" to softDepend.orNull?.ifEmpty { null },
            "loadbefore" to loadBefore.orNull?.ifEmpty { null },
            "commands" to commands.toList().associate {
                it.name to it.toMap()
            }.ifEmpty { null },
            "permissions" to permissions.toList().associate {
                it.name to it.toMap()
            }.ifEmpty { null },
        ).flatMap { (k, v) ->
            if (v != null) {
                listOf(k to v)
            } else emptyList()
        }.toMap()
    }
}
