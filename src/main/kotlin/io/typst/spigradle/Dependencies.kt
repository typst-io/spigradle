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

package io.typst.spigradle

object Dependencies {
    val LOMBOK = Dependency("org.projectlombok", "lombok", "1.18.38")
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