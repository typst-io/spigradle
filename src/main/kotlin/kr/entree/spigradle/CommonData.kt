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

package kr.entree.spigradle

object Dependencies {
    val LOMBOK = Dependency("org.projectlombok", "lombok", "1.18.36")
    val SPIGRADLE = Dependency("kr.entree", "spigradle", SpigradleMeta.VERSION)
    val SPIGRADLE_ANNOTATIONS = Dependency(SPIGRADLE, name = "spigradle-annotations", version = "2.2.0")
    val ALL: List<Dependency> =
        listOf(LOMBOK, SPIGRADLE, SPIGRADLE_ANNOTATIONS)
//    val ALL: List<Pair<String, Dependency>>
//        get() = listOf(
//            Dependencies, SpigotDependencies,
//            BungeeDependencies, NukkitDependencies
//        ).flatMap { it.toFieldEntries() }
}

enum class Repositories(val address: String) {
    SONATYPE("https://oss.sonatype.org/content/repositories/snapshots/"),
    JITPACK("https://jitpack.io"),
//    val ALL: List<Pair<String, String>>
//        get() = listOf(
//            Repositories, SpigotRepositories,
//            BungeeRepositories, NukkitRepositories
//        ).flatMap { it.toFieldEntries() }
}

data class Dependency(
    val group: String,
    val name: String,
    val version: String,
    var isLocal: Boolean = false,
    val versionModifier: (String) -> String = { it },
) {
    fun adjustVersion(inputVersion: String?) = inputVersion?.run(versionModifier) ?: version

    fun format(inputVersion: String? = null): String {
        return "$group:$name:${adjustVersion(inputVersion)}"
    }
}

internal inline fun Dependency(
    dependency: Dependency,
    group: String = dependency.group,
    name: String = dependency.name,
    version: String = dependency.version,
    noinline versionModifier: (String) -> String = dependency.versionModifier,
    isLocal: Boolean = dependency.isLocal,
    configure: Dependency.() -> Unit = {},
) = Dependency(group, name, version, isLocal, versionModifier).apply(configure)

object VersionModifier {
    val SNAPSHOT_APPENDER = createAdjuster("SNAPSHOT")
    val SPIGOT_ADJUSTER = createAdjuster("R0.1", "SNAPSHOT")

    fun createAdjuster(vararg tags: String): (String) -> String = { version ->
        buildString {
            val pieces = version.split("-")
            append(version)
            tags.drop(pieces.size - 1).forEach {
                append("-").append(it)
            }
        }
    }
}