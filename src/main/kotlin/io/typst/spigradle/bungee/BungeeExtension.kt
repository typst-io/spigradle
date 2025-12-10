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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

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
abstract class BungeeExtension @Inject constructor(project: Project) {
    abstract val main: Property<String>
    val name: Property<String> =
        project.objects.property<String>().convention(project.provider { project.name.caseKebabToPascal() })
    val version: Property<String> =
        project.objects.property<String>().convention(project.provider { project.version.toString() })
    val description: Property<String> =
        project.objects.property<String>().convention(project.provider { project.description })
    abstract val author: Property<String>
    abstract val depend: ListProperty<String>
    abstract val softDepend: ListProperty<String>

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
     * Groovy DSL helper for the [author] property.
     */
    fun author(xs: String) {
        author.set(xs)
    }

    /**
     * Groovy DSL helper for the [depend] configuration.
     */
    fun depends(vararg depend: String) {
        this.depend.set(depend.toList())
    }

    /**
     * Groovy DSL helper for the [softDepend] configuration.
     */
    fun softDepends(vararg softDepend: String) {
        this.softDepend.set(softDepend.toList())
    }

    fun toMap(): Map<String, Any> {
        return listOf(
            "main" to main.orNull,
            "name" to name.orNull,
            "version" to version.orNull,
            "description" to description.orNull,
            "author" to author.orNull,
            "depend" to depend.orNull?.ifEmpty { null },
            "softdepend" to softDepend.orNull?.ifEmpty { null }
        ).flatMap { (k, v) ->
            if (v != null) {
                listOf(k to v)
            } else emptyList()
        }.toMap()
    }
}
