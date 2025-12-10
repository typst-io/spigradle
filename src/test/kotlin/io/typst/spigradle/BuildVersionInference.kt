/*
 * Copyright (c) 2021 Spigradle contributors.
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

import io.typst.spigradle.spigot.SpigotDependencies
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test

class BuildVersionInference {
    @Test
    fun gradle() {
        val ver = "0.1.0-R0.1-SNAPSHOT"
        val bigVer = "0.1.1-R0.1-SNAPSHOT"
        val smallVer = "0.0.1-R0.1-SNAPSHOT"
        listOf("compileOnly", "implementation").forEach {
            listOf(
                SpigotDependencies.SPIGOT,
                SpigotDependencies.SPIGOT_API,
                SpigotDependencies.PAPER_API,
            ).forEach { dep ->
                prepareProject().run {
                    dependencies {
                        add(it, dep.format(ver))
                    }
                    dependencies {
                        add(it, dep.format(bigVer))
                    }
                    dependencies {
                        add(it, dep.format(smallVer))
                    }
                }
            }
        }
    }

    fun prepareProject(): Project {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(SpigradlePlugin::class.java)
        project.plugins.apply("java")
        return project
    }
}
