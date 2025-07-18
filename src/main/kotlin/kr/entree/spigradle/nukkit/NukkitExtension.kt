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

package kr.entree.spigradle.nukkit

import kr.entree.spigradle.StandardDescription
import kr.entree.spigradle.spigot.Command
import kr.entree.spigradle.spigot.Load
import kr.entree.spigradle.spigot.Permission
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.newInstance

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
 * import kr.entree.spigradle.spigot.Load
 *
 * spigot {
 *   authors = "Me"
 *   depends = listOf("ProtocolLib", "Vault")
 *   api = listOf("1.0.5")
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
 * See: [https://github.com/NukkitX/ExamplePlugin/blob/master/src/main/resources/plugin.yml#L1]
 */
//@JsonPropertyOrder(
//        "main", "name", "version", "description", "website",
//        "authors", "api", "load", "prefix", "depend",
//        "softdepend", "loadbefore", "commands", "permissions"
//)
open class NukkitExtension(project: Project) : StandardDescription {
    override var main: String? = null
    override var name: String? = null
    override var version: String? = null
    override var description: String? = null
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

    fun authors(authors: Array<String>) {
        this.authors = authors.toList()
    }

    fun api(vararg apis: String) {
        this.api = apis.toList()
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
}
