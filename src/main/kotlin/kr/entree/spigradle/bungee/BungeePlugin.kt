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

package kr.entree.spigradle.bungee

import kr.entree.spigradle.*
import kr.entree.spigradle.annotations.PluginType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.maven

/**
 * The Bungeecord plugin that adds:
 * - [kr.entree.spigradle.YamlGenerate] task for the 'bungee.yml' generation.
 * - [kr.entree.spigradle.SubclassDetection] task for the main-class detection.
 * - Debug tasks for test your plugin.
 */
class BungeePlugin : Plugin<Project> {
    companion object {
        val BUNGEE_TYPE = PluginConvention(
            serverName = "bungee",
            descFile = "bungee.yml",
            mainSuperClass = "net/md_5/bungee/api/plugin/Plugin",
            mainType = PluginType.BUNGEE
        )
    }

    val Project.bungee get() = extensions.getByName<BungeeExtension>("bungee")

    override fun apply(project: Project) {
        with(project) {
            applySpigradlePlugin()
            setupDefaultRepositories()
            registerDescGenTask(BUNGEE_TYPE, BungeeExtension::class.java) { desc ->
                linkedMapOf(
                    "main" to desc.main,
                    "name" to desc.name,
                    "version" to desc.description,
                    "author" to desc.author,
                    "depend" to desc.depends,
                    "softdepend" to desc.softDepends
                ).filterValues {
                    it != null
                }
            }
            createRunConfigurations("Bungee", bungee.debug)
        }
    }

    private fun Project.setupDefaultRepositories() {
        repositories.maven(Repositories.SONATYPE)
    }
}
