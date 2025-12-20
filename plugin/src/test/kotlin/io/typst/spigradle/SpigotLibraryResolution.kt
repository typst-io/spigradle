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

import io.typst.spigradle.catalog.PaperDependencies
import io.typst.spigradle.spigot.SpigotPlugin
import io.typst.spigradle.util.testGradleTask
import org.junit.jupiter.api.io.TempDir
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SpigotLibraryResolution {
    @Test
    fun `resolve libraries and serialize`(@TempDir dir: File) {
        val okhttp = "com.squareup.okhttp3:okhttp:4.9.0"
        testGradleTask(
            SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME, dir, """
            plugins {
                id 'java'
                id 'io.typst.spigradle.spigot'
            }
            
            repositories {
                mavenCentral()
            }
            
            spigot {
                main = 'MyMain'
            }
            
            dependencies {
                compileOnly('${PaperDependencies.SPIGOT_API.format("1.20.1")}')
                compileOnlySpigot("$okhttp")
            }
        """.trimIndent()
        )
        val ymlFile = dir.resolve("build").resolve("tmp").resolve(SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
            .resolve("plugin.yml")
        val settings = LoadSettings.builder()
            .build()
        val yaml = Load(settings).loadFromString(ymlFile.readText()) as Map<String, Any?>
        val libs = yaml["libraries"] as? List<*>
        assertEquals(okhttp, libs?.get(0))
        assertEquals(1, libs?.size ?: 0)
    }

    @Test
    fun `ignore resolution if the property presented`(@TempDir dir: File) {
        val dep = "me.mygroup:myname:1.0.0"
        testGradleTask(
            SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME, dir, """
            plugins {
                id 'java'
                id 'io.typst.spigradle.spigot'
            }
            
            repositories {
                mavenCentral()
            }
            
            spigot {
                main = 'MyMain'
                libraries = ['${dep}']
            }
            
            dependencies {
                compileOnly("${PaperDependencies.SPIGOT_API.format("1.18.1")}")
                implementation("com.squareup.okhttp3:okhttp:4.9.0")
            }
        """.trimIndent()
        )
        val ymlFile = dir.resolve("build").resolve("tmp").resolve(SpigotPlugin.GENERATE_PLUGIN_DESCRIPTION_TASK_NAME)
            .resolve("plugin.yml")
        val load = Load(LoadSettings.builder().build())
        val yaml = load.loadFromString(ymlFile.readText()) as Map<String, Any>
        val libs = yaml["libraries"] as? List<*>
        assertEquals(dep, libs?.get(0))
        assertEquals(1, libs?.size ?: 0)
    }
}
